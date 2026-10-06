import { useState } from "react";

const GOALS = [
  "INCOME_STABILITY",
  "CAPITAL_PRESERVATION",
  "GROWTH",
  "INFLATION_PROTECTION",
  "LEGACY",
  "LIQUIDITY"
];

const RISK_APPETITES = ["CAUTIOUS", "MODERATE", "BALANCED", "ADVENTUROUS"];

function label(value) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

export default function RetirementJourneyForm({ form, onChange, onPartnerChange, onToggleGoal, onSubmit, busy }) {
  const [showSecondPartner, setShowSecondPartner] = useState(true);

  function handleSubmit(event) {
    onSubmit(event, showSecondPartner);
  }

  return (
    <form className="card" onSubmit={handleSubmit}>
      <h2>Household details</h2>
      <div className="grid">
        <label>
          Household name
          <input name="householdName" value={form.householdName} onChange={onChange} />
        </label>
        <label>
          Target annual income (EUR)
          <input
            name="targetAnnualRetirementIncomeEuros"
            type="number"
            value={form.targetAnnualRetirementIncomeEuros}
            onChange={onChange}
          />
        </label>
        <label>
          Essential annual spend (EUR)
          <input
            name="essentialAnnualSpendEuros"
            type="number"
            value={form.essentialAnnualSpendEuros}
            onChange={onChange}
          />
        </label>
        <label>
          Emergency cash buffer (EUR)
          <input
            name="emergencyCashBufferEuros"
            type="number"
            value={form.emergencyCashBufferEuros}
            onChange={onChange}
          />
        </label>
        <label>
          Planning horizon (years)
          <input name="planningHorizonYears" type="number" value={form.planningHorizonYears} onChange={onChange} />
        </label>
        <label>
          Assumed inflation (%)
          <input
            name="assumedAnnualInflationPercent"
            type="number"
            step="0.1"
            value={form.assumedAnnualInflationPercent}
            onChange={onChange}
          />
        </label>
        <label>
          Risk appetite
          <select name="riskAppetite" value={form.riskAppetite} onChange={onChange}>
            {RISK_APPETITES.map((appetite) => (
              <option key={appetite} value={appetite}>
                {label(appetite)}
              </option>
            ))}
          </select>
        </label>
      </div>

      <label className="product-option">
        <input
          type="checkbox"
          name="wantsGuaranteedIncome"
          checked={form.wantsGuaranteedIncome}
          onChange={onChange}
        />
        <span className="product-option-text">We want part of our income guaranteed for life</span>
      </label>

      <h3>Retirement goals</h3>
      <div className="grid">
        {GOALS.map((goal) => (
          <label className="product-option" key={goal}>
            <input type="checkbox" checked={form.goals.includes(goal)} onChange={() => onToggleGoal(goal)} />
            <span className="product-option-text">{label(goal)}</span>
          </label>
        ))}
      </div>

      {["primaryPartner", "secondaryPartner"].map((partnerKey) => {
        if (partnerKey === "secondaryPartner" && !showSecondPartner) {
          return null;
        }
        const partner = form[partnerKey];
        return (
          <section key={partnerKey}>
            <h3>{partnerKey === "primaryPartner" ? "Partner 1" : "Partner 2"}</h3>
            <div className="grid">
              <label>
                Name
                <input
                  value={partner.name}
                  onChange={(event) => onPartnerChange(partnerKey, "name", event.target.value)}
                />
              </label>
              <label>
                Current age
                <input
                  type="number"
                  value={partner.currentAge}
                  onChange={(event) => onPartnerChange(partnerKey, "currentAge", event.target.value)}
                />
              </label>
              <label>
                Retirement age
                <input
                  type="number"
                  value={partner.retirementAge}
                  onChange={(event) => onPartnerChange(partnerKey, "retirementAge", event.target.value)}
                />
              </label>
              <label>
                Pension fund value (EUR)
                <input
                  type="number"
                  value={partner.pensionFundValueEuros}
                  onChange={(event) => onPartnerChange(partnerKey, "pensionFundValueEuros", event.target.value)}
                />
              </label>
              <label>
                Monthly contribution (EUR)
                <input
                  type="number"
                  value={partner.monthlyContributionEuros}
                  onChange={(event) => onPartnerChange(partnerKey, "monthlyContributionEuros", event.target.value)}
                />
              </label>
              <label>
                State pension per year (EUR)
                <input
                  type="number"
                  value={partner.expectedStatePensionAnnualEuros}
                  onChange={(event) =>
                    onPartnerChange(partnerKey, "expectedStatePensionAnnualEuros", event.target.value)
                  }
                />
              </label>
            </div>
          </section>
        );
      })}

      <label className="product-option">
        <input
          type="checkbox"
          checked={showSecondPartner}
          onChange={(event) => setShowSecondPartner(event.target.checked)}
        />
        <span className="product-option-text">Include a second partner in the plan</span>
      </label>

      <button type="submit" disabled={busy}>
        {busy ? "Agents are working..." : "Generate retirement journey"}
      </button>
    </form>
  );
}