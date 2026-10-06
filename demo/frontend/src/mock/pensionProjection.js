/**
 * Deterministic mock pension fund projection.
 *
 * Real projections would come from the backend; this derives stable, plausible figures
 * from the policy id so the demo page shows consistent values between visits.
 */

const EUR = new Intl.NumberFormat("en-IE", {
  style: "currency",
  currency: "EUR",
  maximumFractionDigits: 0
});

export function formatEuros(value) {
  return EUR.format(Math.round(value));
}

/** Simple stable hash so the same policy always yields the same mock figures. */
function hash(text) {
  let value = 0;
  for (let i = 0; i < text.length; i += 1) {
    value = (value * 31 + text.charCodeAt(i)) >>> 0;
  }
  return value;
}

function pick(seed, min, max, step = 1) {
  const span = Math.floor((max - min) / step) + 1;
  return min + (seed % span) * step;
}

/**
 * @param policyId  policy identifier, used as the deterministic seed
 * @param policy    optional real policy record (used for the maturity date when available)
 */
export function buildMockProjection(policyId, policy) {
  const seed = hash(policyId || "POLICY");

  const currentFundValueEuros = pick(seed, 85000, 320000, 5000);
  const monthlyContributionEuros = pick(seed >> 3, 200, 900, 50);
  const annualGrowthRatePercent = pick(seed >> 7, 30, 65) / 10; // 3.0% - 6.5%
  const annualManagementFeePercent = pick(seed >> 11, 50, 110) / 100; // 0.50% - 1.10%

  const today = new Date();
  const maturityDate = policy?.maturityDate ? new Date(policy.maturityDate) : null;

  const yearsToMaturity = maturityDate
    ? Math.max(0, Math.round((maturityDate - today) / (365.25 * 24 * 60 * 60 * 1000)))
    : pick(seed >> 13, 1, 12);

  // Year-by-year: contribute, grow at the gross rate, then deduct the management fee.
  const yearlyProjection = [];
  let balance = currentFundValueEuros;
  let totalContributions = 0;
  let totalFees = 0;

  for (let year = 1; year <= Math.max(yearsToMaturity, 1); year += 1) {
    const contributions = monthlyContributionEuros * 12;
    const opening = balance;

    balance += contributions;
    const growth = balance * (annualGrowthRatePercent / 100);
    balance += growth;

    const fee = balance * (annualManagementFeePercent / 100);
    balance -= fee;

    totalContributions += contributions;
    totalFees += fee;

    yearlyProjection.push({
      year: today.getFullYear() + year,
      yearsFromNow: year,
      openingBalanceEuros: opening,
      contributionsEuros: contributions,
      growthEuros: growth,
      feesEuros: fee,
      closingBalanceEuros: balance
    });
  }

  const projectedValueAtMaturityEuros = balance;
  // Illustrative 4% sustainable drawdown, plus a 25% tax-free lump sum.
  const taxFreeLumpSumEuros = projectedValueAtMaturityEuros * 0.25;
  const estimatedAnnualIncomeEuros = (projectedValueAtMaturityEuros - taxFreeLumpSumEuros) * 0.04;

  return {
    policyId,
    currentFundValueEuros,
    monthlyContributionEuros,
    annualGrowthRatePercent,
    annualManagementFeePercent,
    yearsToMaturity,
    maturityDate: policy?.maturityDate ?? null,
    totalContributionsEuros: totalContributions,
    totalFeesEuros: totalFees,
    projectedValueAtMaturityEuros,
    taxFreeLumpSumEuros,
    estimatedAnnualIncomeEuros,
    yearlyProjection
  };
}