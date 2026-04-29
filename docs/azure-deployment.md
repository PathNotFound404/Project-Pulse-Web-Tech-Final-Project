/# Azure Deployment Guide — Project Pulse

This guide covers every manual step required to provision Azure infrastructure, configure secrets, and trigger the CI/CD pipeline for Project Pulse.

---

## Prerequisites

Install and authenticate the following tools before starting:

- [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) (`az login`)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- GitHub repository admin access (to add Actions secrets)

Log in to Azure:

```bash
az login
az account set --subscription "<your-subscription-id>"
```

---

## 1. Create a Resource Group

All Project Pulse resources live in one group for easy management and teardown.

**Azure Portal:**
1. Go to **Resource groups** → **Create**
2. Subscription: your subscription
3. Resource group name: `project-pulse-rg`
4. Region: `East US` (or nearest to your users)
5. Click **Review + create** → **Create**

**Azure CLI equivalent:**
```bash
az group create --name project-pulse-rg --location eastus
```

---

## 2. Create Azure Container Registry (ACR)

ACR stores the Docker images that GitHub Actions builds and Azure App Service pulls.

**Azure Portal:**
1. Search **Container registries** → **Create**
2. Resource group: `project-pulse-rg`
3. Registry name: `projectpulseacr` *(must be globally unique — add initials if taken)*
4. Location: same as resource group
5. Pricing plan: **Basic**
6. Click **Review + create** → **Create**

**Enable Admin user** (needed for GitHub Actions login):
1. Open the registry → **Settings → Access keys**
2. Toggle **Admin user** to **Enabled**
3. Note the **Login server**, **Username**, and **Password** — you'll need these for GitHub Secrets

**Azure CLI equivalent:**
```bash
az acr create \
  --resource-group project-pulse-rg \
  --name projectpulseacr \
  --sku Basic \
  --admin-enabled true
```

---

## 3. Create Azure Database for MySQL Flexible Server

**Azure Portal:**
1. Search **Azure Database for MySQL flexible servers** → **Create**
2. Resource group: `project-pulse-rg`
3. Server name: `projectpulse-db` *(globally unique)*
4. Region: same as resource group
5. MySQL version: **8.0**
6. Workload type: **Development** (selects Burstable B1ms — cheapest tier)
7. Admin username: choose one (e.g., `pulseadmin`) — **save this**
8. Password: choose a strong password — **save this**
9. **Networking tab**: set Connectivity method to **Public access**
   - Check **Allow public access from any Azure service** *(needed for App Service connection)*
   - Optionally add your own IP for local access
10. Click **Review + create** → **Create** (takes ~3 minutes)

**Create the database:**
After the server is provisioned, go to the server → **Databases** → **Add**:
- Database name: `projectpulse`
- Click **Save**

**Note the JDBC URL** for Key Vault — it will be:
```
jdbc:mysql://projectpulse-db.mysql.database.azure.com:3306/projectpulse?useSSL=true&requireSSL=false&serverTimezone=UTC
```

**Azure CLI equivalent:**
```bash
az mysql flexible-server create \
  --resource-group project-pulse-rg \
  --name projectpulse-db \
  --admin-user pulseadmin \
  --admin-password "<your-password>" \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --version 8.0 \
  --public-access 0.0.0.0

az mysql flexible-server db create \
  --resource-group project-pulse-rg \
  --server-name projectpulse-db \
  --database-name projectpulse
```

---

## 4. Create Azure Key Vault

Key Vault stores database credentials that the backend App Service reads at runtime via Managed Identity — no secrets in code or environment variables.

**Azure Portal:**
1. Search **Key vaults** → **Create**
2. Resource group: `project-pulse-rg`
3. Key vault name: `projectpulse-kv` *(globally unique)*
4. Region: same as resource group
5. Pricing tier: **Standard**
6. **Access configuration tab**: Permission model → **Vault access policy** *(simpler for App Service)*
7. Click **Review + create** → **Create**

**Add secrets** (Key Vault → **Objects → Secrets → Generate/Import**):

| Secret name | Value |
|---|---|
| `db-url` | `jdbc:mysql://projectpulse-db.mysql.database.azure.com:3306/projectpulse?useSSL=true&requireSSL=false&serverTimezone=UTC` |
| `db-username` | your MySQL admin username (e.g., `pulseadmin`) |
| `db-password` | your MySQL admin password |
| `cors-allowed-origin` | `https://projectpulse-frontend.azurewebsites.net` *(fill in after frontend app is created)* |

For each secret:
1. **Upload options**: Manual
2. **Name**: exact name from table above
3. **Value**: the value
4. Click **Create**

After creating each secret, click it → click the current version → copy the **Secret Identifier URI** (format: `https://projectpulse-kv.vault.azure.net/secrets/<name>/<version-id>`). You'll need these for App Service config references.

**Azure CLI equivalent:**
```bash
az keyvault create \
  --resource-group project-pulse-rg \
  --name projectpulse-kv \
  --location eastus

az keyvault secret set --vault-name projectpulse-kv --name db-url \
  --value "jdbc:mysql://projectpulse-db.mysql.database.azure.com:3306/projectpulse?useSSL=true&requireSSL=false&serverTimezone=UTC"

az keyvault secret set --vault-name projectpulse-kv --name db-username --value "pulseadmin"
az keyvault secret set --vault-name projectpulse-kv --name db-password --value "<your-password>"
az keyvault secret set --vault-name projectpulse-kv --name cors-allowed-origin \
  --value "https://projectpulse-frontend.azurewebsites.net"
```

---

## 5. Create an App Service Plan

Both web apps share one Linux plan.

**Azure Portal:**
1. Search **App Service plans** → **Create**
2. Resource group: `project-pulse-rg`
3. Name: `projectpulse-plan`
4. Operating system: **Linux**
5. Region: same as resource group
6. Pricing plan: **Basic B1** *(cheapest plan that supports custom domains and always-on)*
7. Click **Review + create** → **Create**

**Azure CLI equivalent:**
```bash
az appservice plan create \
  --resource-group project-pulse-rg \
  --name projectpulse-plan \
  --is-linux \
  --sku B1
```

---

## 6. Create the Backend Web App

**Azure Portal:**
1. Search **App Services** → **Create** → **Web App**
2. Resource group: `project-pulse-rg`
3. Name: `projectpulse-backend` *(becomes the subdomain — must be globally unique)*
4. Publish: **Container**
5. Operating system: **Linux**
6. Region: same as resource group
7. App Service Plan: `projectpulse-plan`
8. **Container tab**:
   - Image source: **Azure Container Registry**
   - Registry: `projectpulseacr`
   - Image: `projectpulse-backend`
   - Tag: `latest`
9. Click **Review + create** → **Create**

### 6a. Enable System-Assigned Managed Identity

The backend uses Managed Identity to authenticate to Key Vault without storing credentials.

1. Open `projectpulse-backend` App Service
2. **Settings → Identity**
3. **System assigned** tab → toggle Status to **On** → **Save** → confirm
4. Copy the **Object (principal) ID** that appears

### 6b. Grant Key Vault Access to the Backend

1. Open `projectpulse-kv` Key Vault
2. **Access policies** → **Create**
3. **Permissions tab**: under Secret permissions, check **Get** and **List**
4. **Principal tab**: search for `projectpulse-backend` (the App Service identity)
5. Click **Next** → **Next** → **Create**

**Azure CLI equivalent:**
```bash
# Get the identity principal ID
PRINCIPAL_ID=$(az webapp identity show \
  --resource-group project-pulse-rg \
  --name projectpulse-backend \
  --query principalId -o tsv)

az keyvault set-policy \
  --name projectpulse-kv \
  --object-id $PRINCIPAL_ID \
  --secret-permissions get list
```

### 6c. Configure App Settings (Key Vault References)

Key Vault references let App Service inject secrets as environment variables at runtime.

1. Open `projectpulse-backend` App Service → **Settings → Environment variables**
2. Add the following **Application settings** (click **Add** for each):

| Name | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `@Microsoft.KeyVault(SecretUri=https://projectpulse-kv.vault.azure.net/secrets/db-url/<version-id>/)` |
| `SPRING_DATASOURCE_USERNAME` | `@Microsoft.KeyVault(SecretUri=https://projectpulse-kv.vault.azure.net/secrets/db-username/<version-id>/)` |
| `SPRING_DATASOURCE_PASSWORD` | `@Microsoft.KeyVault(SecretUri=https://projectpulse-kv.vault.azure.net/secrets/db-password/<version-id>/)` |
| `APP_CORS_ALLOWED_ORIGIN` | `@Microsoft.KeyVault(SecretUri=https://projectpulse-kv.vault.azure.net/secrets/cors-allowed-origin/<version-id>/)` |
| `APP_FRONTEND_BASE_URL` | `https://projectpulse-frontend.azurewebsites.net` |
| `WEBSITES_PORT` | `8080` |

> **Important**: Replace `<version-id>` in each URI with the actual version ID copied from Key Vault, or use the versionless URI format: `https://projectpulse-kv.vault.azure.net/secrets/db-url/` (omit the version ID entirely — Azure always resolves to the latest version).

3. Click **Apply** → **Confirm**

### 6d. Get the Backend Publish Profile

GitHub Actions uses this to deploy.

1. Open `projectpulse-backend` App Service
2. Click **Download publish profile** (top menu bar)
3. Open the downloaded `.PublishSettings` file in a text editor — copy the entire XML content

You'll add this to GitHub Secrets in step 8.

---

## 7. Create the Frontend Web App

**Azure Portal:**
1. Search **App Services** → **Create** → **Web App**
2. Resource group: `project-pulse-rg`
3. Name: `projectpulse-frontend` *(globally unique)*
4. Publish: **Container**
5. Operating system: **Linux**
6. Region: same as resource group
7. App Service Plan: `projectpulse-plan`
8. **Container tab**:
   - Image source: **Azure Container Registry**
   - Registry: `projectpulseacr`
   - Image: `projectpulse-frontend`
   - Tag: `latest`
9. Click **Review + create** → **Create**

The frontend has no Key Vault needs — `VITE_API_BASE_URL` is baked into the image at Docker build time via `--build-arg`.

### 7a. Get the Frontend Publish Profile

Same as backend:
1. Open `projectpulse-frontend` App Service
2. Click **Download publish profile**
3. Copy the entire XML content of the downloaded file

---

## 8. Configure GitHub Actions Secrets

Go to your GitHub repository → **Settings → Secrets and variables → Actions → New repository secret**. Add each of the following:

| Secret name | Value |
|---|---|
| `ACR_LOGIN_SERVER` | `projectpulseacr.azurecr.io` |
| `ACR_USERNAME` | ACR admin username (from ACR → Access keys) |
| `ACR_PASSWORD` | ACR admin password (from ACR → Access keys) |
| `BACKEND_APP_NAME` | `projectpulse-backend` |
| `FRONTEND_APP_NAME` | `projectpulse-frontend` |
| `BACKEND_PUBLISH_PROFILE` | Full XML content from backend publish profile download |
| `FRONTEND_PUBLISH_PROFILE` | Full XML content from frontend publish profile download |
| `BACKEND_URL` | `https://projectpulse-backend.azurewebsites.net` |

---

## 9. How the CI/CD Pipeline Works

### CI (`ci.yml`) — runs on every pull request to `main`

| Job | What it does |
|---|---|
| `backend-test` | Checks out code → sets up Java 17 → runs `mvn test` (uses H2 in-memory DB, no Azure needed) |
| `frontend-build` | Checks out code → sets up Node 20 → `npm ci` → `npm run build` with a placeholder backend URL |

A PR cannot be merged if either job fails.

### Deploy (`deploy.yml`) — runs on every push to `main`

| Job | Steps |
|---|---|
| `deploy-backend` | ACR login → `docker build` in `./backend` → push `:sha` and `:latest` tags → `azure/webapps-deploy` pulls the image |
| `deploy-frontend` | ACR login → `docker build --build-arg VITE_API_BASE_URL=...` in `./frontend` → push tags → `azure/webapps-deploy` pulls the image |

The two jobs run in parallel. Each App Service automatically restarts with the new container after deployment completes (~2–4 minutes total from push to live).

---

## 10. Triggering the First Deployment

Merge any branch to `main` (or push directly) to trigger the deploy workflow:

```bash
git checkout main
git merge feature/your-branch
git push origin main
```

Monitor progress at: **GitHub repo → Actions → Deploy**

---

## 11. Verification Checklist

After the deploy workflow goes green, verify each layer:

- [ ] **Backend health**: `GET https://projectpulse-backend.azurewebsites.net/api/sections` returns a JSON array (proves: app started, DB connected, JPA schema created)
- [ ] **Key Vault resolution**: In App Service → Environment variables, each Key Vault reference shows a green checkmark (not a red warning icon)
- [ ] **Frontend loads**: Open `https://projectpulse-frontend.azurewebsites.net` — login page renders correctly
- [ ] **Login works**: Log in as an instructor or admin — data loads from backend
- [ ] **CORS working**: No CORS errors in browser DevTools when frontend calls backend APIs
- [ ] **Vue Router**: Navigate directly to a deep URL (e.g., `/instructor/reports`) — page loads (not 404), confirming Nginx SPA fallback works

---

## 12. Troubleshooting

### App Service shows "Application Error" or blank page

1. Open App Service → **Monitoring → Log stream** to see live container output
2. Or go to **Diagnose and solve problems → Application Logs**

### Key Vault reference shows red icon in App Settings

The Managed Identity doesn't have access. Verify:
1. System-assigned identity is enabled on the App Service
2. The Key Vault access policy includes the App Service's principal ID with **Get** and **List** secret permissions
3. The Secret URI in the app setting is correctly formatted

### CORS errors in browser console

The `APP_CORS_ALLOWED_ORIGIN` setting must exactly match the frontend origin (no trailing slash):
- Correct: `https://projectpulse-frontend.azurewebsites.net`
- Wrong: `https://projectpulse-frontend.azurewebsites.net/`

Update the `cors-allowed-origin` secret in Key Vault, then restart the backend App Service.

### Backend can't connect to MySQL

1. Check the MySQL server firewall: **MySQL server → Networking** → ensure **Allow public access from any Azure service** is checked
2. Verify the JDBC URL uses the full hostname: `projectpulse-db.mysql.database.azure.com`
3. Check `SPRING_DATASOURCE_USERNAME` format — MySQL Flexible Server requires just the username (e.g., `pulseadmin`), not `pulseadmin@projectpulse-db`
4. Ensure the database `projectpulse` exists on the server

### GitHub Actions fails at `docker build`

- Verify `ACR_LOGIN_SERVER`, `ACR_USERNAME`, `ACR_PASSWORD` secrets are set correctly
- Confirm Admin user is enabled on the ACR (ACR → Access keys)

### Frontend shows old data / stale build

The Nginx container caches static assets aggressively (1-year cache headers). Hard-refresh (`Ctrl+Shift+R`) or clear browser cache. Vite bundles include content hashes in filenames, so a fresh deployment always serves the new files on first load.

### Cold start delays

Free and Basic tier App Services may take 30–60 seconds on the first request after a period of inactivity. This is normal — the container needs to start. Upgrade to Standard tier and enable **Always On** if cold starts are unacceptable.

---

## Resource Summary

| Resource | Name | Purpose |
|---|---|---|
| Resource Group | `project-pulse-rg` | Container for all resources |
| Container Registry | `projectpulseacr` | Docker image storage |
| MySQL Flexible Server | `projectpulse-db` | Production database |
| Database | `projectpulse` | Application schema |
| Key Vault | `projectpulse-kv` | DB credentials, CORS origin |
| App Service Plan | `projectpulse-plan` | Shared Linux hosting plan |
| App Service (Backend) | `projectpulse-backend` | Spring Boot API container |
| App Service (Frontend) | `projectpulse-frontend` | Nginx + Vue 3 SPA container |
