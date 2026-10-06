# Pension Product Comparison API

This Spring Boot project includes a REST solution for customers to compare pension products across multiple insurers.

## What is included

- Product catalog endpoint
- Single product lookup endpoint
- Comparison endpoint with weighted customer-focused scoring
- In-memory sample pension products
- Unit and integration tests

## API endpoints

- `GET /api/v1/pension-products/products`
- `GET /api/v1/pension-products/{id}`
- `POST /api/v1/pension-products/compare`

With the configured context path, all URLs are prefixed by `/demo`.

### Example compare request

```json
{
  "productIds": ["PEN-1001", "PEN-1002", "PEN-1003"],
  "currentAge": 35,
  "retirementAge": 65,
  "initialFundValueEuros": 50000,
  "regularMonthlyContributionEuros": 400,
  "singleAnnualContributionEuros": 2000,
  "compoundAnnualGrowthRateBeforePrsaFeesPercent": 5.5,
  "weights": {
    "feeWeight": 0.4,
    "creditRatingWeight": 0.3,
    "flexibilityWeight": 0.2,
    "digitalServicesWeight": 0.1
  }
}
```

Products are compared by:

- Fund management fee impact until retirement using contributions and pre-fee growth assumptions
- Financial robustness of insurer (credit rating)
- Flexibility of fund selection
- Digital services quality

Compare response includes, for each ranked provider/product:

- `projectedFeeCostUntilRetirementEuros`
- `projectedFundValueAtRetirementEuros`

The value from `compoundAnnualGrowthRateBeforePrsaFeesPercent` is user-set and applied consistently to each provider projection.

## Run and test

```bash
./mvnw spring-boot:run
./mvnw test
```

For Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

## React web app

A React frontend is available in `frontend/`. On the home page it fetches all products and lets the user select products and compare with customer criteria.

Run the frontend in a separate terminal:

```powershell
Set-Location "C:\Users\PednekM\Documents\IntellijProjects\demo\demo\frontend"
npm install
npm run dev
```

Run frontend tests:

```powershell
Set-Location "C:\Users\PednekM\Documents\IntellijProjects\demo\demo\frontend"
npm test
```

## Agentic Retirement Journey

An agent-driven module builds a retirement journey and reinvestment plan for partners who are
about a year away from retirement.

### Agent crew

Agents implement `RetirementAgent` and are auto-discovered by Spring, then executed in `order()`
sequence by `RetirementJourneyOrchestrator` over a shared `AgentContext` blackboard:

1. **Household Profile Agent** - normalises partner data and projects the combined pot at the retirement date.
2. **Risk Profiling Agent** - reconciles stated appetite with real risk capacity and sets the growth-asset ceiling.
3. **Income & Drawdown Agent** - models lump sum, sustainable drawdown, income gap and pot longevity.
4. **Reinvestment Allocation Agent** - designs liquidity / stability / growth (and optional annuity) buckets.
5. **Compliance & Guardrail Agent** - applies tax, drawdown and suitability guardrails.
6. **Journey Planner Agent** - sequences the final pre-retirement year and the first years of drawdown.

Each agent returns an `AgentInsight`, so the full reasoning trace is returned to the UI.

### Endpoints

- `GET /api/v1/retirement-journey/agents`
- `POST /api/v1/retirement-journey/plan`

With the configured context path, the plan URL is `POST /demo/api/v1/retirement-journey/plan`.

### Example plan request

```json
{
  "householdName": "Murphy household",
  "primaryPartner": {
    "name": "Aoife",
    "currentAge": 64,
    "retirementAge": 65,
    "pensionFundValueEuros": 420000,
    "monthlyContributionEuros": 800,
    "expectedStatePensionAnnualEuros": 14000
  },
  "secondaryPartner": {
    "name": "Liam",
    "currentAge": 65,
    "retirementAge": 66,
    "pensionFundValueEuros": 310000,
    "monthlyContributionEuros": 600,
    "expectedStatePensionAnnualEuros": 14000
  },
  "targetAnnualRetirementIncomeEuros": 55000,
  "essentialAnnualSpendEuros": 36000,
  "emergencyCashBufferEuros": 25000,
  "riskAppetite": "MODERATE",
  "goals": ["INCOME_STABILITY", "INFLATION_PROTECTION"],
  "planningHorizonYears": 30,
  "assumedAnnualInflationPercent": 2.0,
  "wantsGuaranteedIncome": true
}
```

The response contains the plan summary, income projection, bucketed reinvestment allocation,
a 7-step journey, guardrails, next best actions and the agent reasoning trace.

### UI

The React app is now tabbed: **Compare products** and **Retirement journey**
(`frontend/src/pages/RetirementJourneyPage.jsx`). The journey tab lists the agent crew, captures both
partners' details and renders the generated plan, timeline and agent trace.

> Projections are illustrative only and do not constitute financial advice.