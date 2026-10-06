import { useEffect, useState } from "react";
import { createRetirementPlan, fetchAgents } from "../api/retirementApi";
import RetirementJourneyForm from "../components/RetirementJourneyForm";
import RetirementPlanResults from "../components/RetirementPlanResults";

const defaultForm = {
  householdName: "Murphy household",
  targetAnnualRetirementIncomeEuros: 55000,
  essentialAnnualSpendEuros: 36000,
  emergencyCashBufferEuros: 25000,
  planningHorizonYears: 30,
  assumedAnnualInflationPercent: 2,
  riskAppetite: "MODERATE",
  wantsGuaranteedIncome: true,
  goals: ["INCOME_STABILITY", "INFLATION_PROTECTION"],
  primaryPartner: {
    name: "Aoife",
    currentAge: 64,
    retirementAge: 65,
    pensionFundValueEuros: 420000,
    monthlyContributionEuros: 800,
    expectedStatePensionAnnualEuros: 14000
  },
  secondaryPartner: {
    name: "Liam",
    currentAge: 65,
    retirementAge: 66,
    pensionFundValueEuros: 310000,
    monthlyContributionEuros: 600,
    expectedStatePensionAnnualEuros: 14000
  }
};

function toNumber(value) {
  const parsed = Number(value);
  return Number.isNaN(parsed) ? 0 : parsed;
}

function mapPartner(partner) {
  return {
    name: partner.name,
    currentAge: toNumber(partner.currentAge),
    retirementAge: toNumber(partner.retirementAge),
    pensionFundValueEuros: toNumber(partner.pensionFundValueEuros),
    monthlyContributionEuros: toNumber(partner.monthlyContributionEuros),
    expectedStatePensionAnnualEuros: toNumber(partner.expectedStatePensionAnnualEuros)
  };
}

export default function RetirementJourneyPage() {
  const [form, setForm] = useState(defaultForm);
  const [agents, setAgents] = useState([]);
  const [plan, setPlan] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchAgents()
      .then(setAgents)
      .catch(() => setAgents([]));
  }, []);

  function handleChange(event) {
    const { name, value, type, checked } = event.target;
    setForm((previous) => ({
      ...previous,
      [name]: type === "checkbox" ? checked : value
    }));
  }

  function handlePartnerChange(partnerKey, field, value) {
    setForm((previous) => ({
      ...previous,
      [partnerKey]: { ...previous[partnerKey], [field]: value }
    }));
  }

  function toggleGoal(goal) {
    setForm((previous) => ({
      ...previous,
      goals: previous.goals.includes(goal)
        ? previous.goals.filter((item) => item !== goal)
        : [...previous.goals, goal]
    }));
  }

  async function handleSubmit(event, includeSecondPartner) {
    event.preventDefault();

    const payload = {
      householdName: form.householdName,
      primaryPartner: mapPartner(form.primaryPartner),
      secondaryPartner: includeSecondPartner ? mapPartner(form.secondaryPartner) : null,
      targetAnnualRetirementIncomeEuros: toNumber(form.targetAnnualRetirementIncomeEuros),
      essentialAnnualSpendEuros: toNumber(form.essentialAnnualSpendEuros),
      emergencyCashBufferEuros: toNumber(form.emergencyCashBufferEuros),
      riskAppetite: form.riskAppetite,
      goals: form.goals,
      planningHorizonYears: toNumber(form.planningHorizonYears),
      assumedAnnualInflationPercent: toNumber(form.assumedAnnualInflationPercent),
      wantsGuaranteedIncome: Boolean(form.wantsGuaranteedIncome)
    };

    try {
      setBusy(true);
      setError("");
      const result = await createRetirementPlan(payload);
      setPlan(result);
    } catch (e) {
      setError(e.message || "Failed to generate the retirement journey");
    } finally {
      setBusy(false);
    }
  }

  return (
    <section>
      <h1>Agentic Retirement Journey</h1>
      <p>
        A crew of specialised agents profiles your household, sets the risk level, models income, designs the
        reinvestment plan, applies guardrails and sequences the journey.
      </p>

      {agents.length > 0 && (
        <div className="card">
          <h2>Agent crew</h2>
          <ol className="agent-list">
            {agents.map((agent) => (
              <li key={agent.id}>
                <strong>{agent.name}</strong> - {agent.role}
              </li>
            ))}
          </ol>
        </div>
      )}

      {error && <p className="error">{error}</p>}

      <RetirementJourneyForm
        form={form}
        onChange={handleChange}
        onPartnerChange={handlePartnerChange}
        onToggleGoal={toggleGoal}
        onSubmit={handleSubmit}
        busy={busy}
      />

      <RetirementPlanResults plan={plan} />
    </section>
  );
}