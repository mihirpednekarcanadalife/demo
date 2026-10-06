export default function CriteriaForm({ form, onChange, onSubmit, disabled }) {
  return (
    <form className="card" onSubmit={onSubmit}>
      <h2>Comparison Criteria</h2>
      <div className="grid">
        <label>
          Current Age
          <input name="currentAge" type="number" value={form.currentAge} onChange={onChange} min="18" max="75" />
        </label>
        <label>
          Retirement Age
          <input name="retirementAge" type="number" value={form.retirementAge} onChange={onChange} min="19" max="80" />
        </label>
        <label>
          Initial Fund Value (EUR)
          <input name="initialFundValueEuros" type="number" value={form.initialFundValueEuros} onChange={onChange} min="0" step="100" />
        </label>
        <label>
          Regular Monthly Contribution (EUR)
          <input name="regularMonthlyContributionEuros" type="number" value={form.regularMonthlyContributionEuros} onChange={onChange} min="0" step="10" />
        </label>
        <label>
          Single Annual Contribution (EUR)
          <input name="singleAnnualContributionEuros" type="number" value={form.singleAnnualContributionEuros} onChange={onChange} min="0" step="10" />
        </label>
        <label>
          CAGR Before PRSA Fees (%)
          <input
            name="compoundAnnualGrowthRateBeforePrsaFeesPercent"
            type="number"
            value={form.compoundAnnualGrowthRateBeforePrsaFeesPercent}
            onChange={onChange}
            step="0.1"
          />
        </label>
      </div>

      <h3>Weights</h3>
      <div className="grid">
        <label>
          Fee Weight
          <input name="feeWeight" type="number" value={form.feeWeight} onChange={onChange} min="0" step="0.05" />
        </label>
        <label>
          Credit Rating Weight
          <input name="creditRatingWeight" type="number" value={form.creditRatingWeight} onChange={onChange} min="0" step="0.05" />
        </label>
        <label>
          Flexibility Weight
          <input name="flexibilityWeight" type="number" value={form.flexibilityWeight} onChange={onChange} min="0" step="0.05" />
        </label>
        <label>
          Digital Services Weight
          <input name="digitalServicesWeight" type="number" value={form.digitalServicesWeight} onChange={onChange} min="0" step="0.05" />
        </label>
      </div>

      <button type="submit" disabled={disabled}>Compare Products</button>
    </form>
  );
}