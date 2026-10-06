# React Pension Comparison Web App

This React app calls the Spring Boot pension comparison API and lets customers:

- Load all pension products on the home page
- Select products to compare
- Enter customer criteria and projection assumptions
- View ranked comparison results and retirement projections

## API base URL

By default, the app calls:

- `http://localhost:8080/demo/api/v1/pension-products`

You can override it with:

- `VITE_API_BASE_URL`

## Run

```powershell
Set-Location "C:\Users\PednekM\Documents\IntellijProjects\demo\demo\frontend"
npm install
npm run dev
```

## Test

```powershell
Set-Location "C:\Users\PednekM\Documents\IntellijProjects\demo\demo\frontend"
npm test
```