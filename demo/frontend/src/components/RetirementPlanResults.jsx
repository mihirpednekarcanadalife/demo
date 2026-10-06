const euro = new Intl.NumberFormat("en-IE", {
  style: "currency",
  currency: "EUR",
  maximumFractionDigits: 0
});

export default function RetirementPlanResults({ plan }) {
  if (!plan) {
    return null;
  }

  const projection = plan.incomeProjection || {};

  return (
    <section>
      <div className="card">
        <h2>Plan summary</h2>
        <p>{plan.planSummary}</p>
        <div className="grid">
          <div className="stat">
            <span className="stat-label">Risk profile</span>
            <strong>{plan.derivedRiskProfile}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Years to retirement</span>
            <strong>{plan.yearsToRetirement}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Projected annual income</span>
            <strong>{euro.format(projection.totalProjectedAnnualIncomeEuros || 0)}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Target coverage</span>
            <strong>{projection.targetCoveragePercent}%</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Pot longevity</span>
            <strong>{projection.potLongevityYears} years</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Agent confidence</span>
            <strong>{Math.round((plan.planConfidence || 0) * 100)}%</strong>
          </div>
        </div>
      </div>

      <div className="card">
        <h2>Income projection</h2>
        <div className="table-wrap">
          <table>
            <tbody>
              <tr>
                <th>Combined pension pot</th>
                <td>{euro.format(projection.combinedPensionPotEuros || 0)}</td>
              </tr>
              <tr>
                <th>Tax-free lump sum</th>
                <td>{euro.format(projection.taxFreeLumpSumEuros || 0)}</td>
              </tr>
              <tr>
                <th>Reinvestable pot</th>
                <td>{euro.format(projection.reinvestablePotEuros || 0)}</td>
              </tr>
              <tr>
                <th>Sustainable drawdown</th>
                <td>{euro.format(projection.sustainableAnnualDrawdownEuros || 0)} per year</td>
              </tr>
              <tr>
                <th>State pension</th>
                <td>{euro.format(projection.statePensionAnnualEuros || 0)} per year</td>
              </tr>
              <tr>
                <th>Income gap</th>
                <td className={projection.annualIncomeGapEuros > 0 ? "error" : "success"}>
                  {euro.format(projection.annualIncomeGapEuros || 0)} per year
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <h2>Reinvestment plan</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Bucket</th>
                <th>Vehicle</th>
                <th>Allocation</th>
                <th>Amount</th>
                <th>Horizon</th>
                <th>Purpose</th>
              </tr>
            </thead>
            <tbody>
              {(plan.reinvestmentAllocation || []).map((slice) => (
                <tr key={slice.bucket}>
                  <td>{slice.bucket}</td>
                  <td>{slice.vehicle}</td>
                  <td>{slice.allocationPercent}%</td>
                  <td>{euro.format(slice.amountEuros)}</td>
                  <td>{slice.timeHorizon}</td>
                  <td>{slice.purpose}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <h2>Retirement journey</h2>
        <ol className="timeline">
          {(plan.journey || []).map((milestone) => (
            <li key={milestone.sequence}>
              <span className="timeline-time">{milestone.timeframe}</span>
              <strong>{milestone.title}</strong>
              <p>{milestone.description}</p>
              <ul>
                {milestone.actions.map((action) => (
                  <li key={action}>{action}</li>
                ))}
              </ul>
            </li>
          ))}
        </ol>
      </div>

      <div className="card">
        <h2>Next best actions</h2>
        <ul>
          {(plan.nextBestActions || []).map((action) => (
            <li key={action}>{action}</li>
          ))}
        </ul>
      </div>

      <div className="card">
        <h2>Guardrails</h2>
        <ul>
          {(plan.guardrails || []).map((guardrail) => (
            <li key={guardrail}>{guardrail}</li>
          ))}
        </ul>
      </div>

      <div className="card">
        <h2>Agent reasoning trace</h2>
        {(plan.agentTrace || []).map((insight) => (
          <details key={insight.agentId} className="agent-trace">
            <summary>
              {insight.order}. {insight.agentName} - {insight.headline} ({Math.round(insight.confidence * 100)}%)
            </summary>
            <p>
              <em>{insight.role}</em>
            </p>
            <strong>Findings</strong>
            <ul>
              {insight.findings.map((finding) => (
                <li key={finding}>{finding}</li>
              ))}
            </ul>
            <strong>Recommended actions</strong>
            <ul>
              {insight.recommendedActions.map((action) => (
                <li key={action}>{action}</li>
              ))}
            </ul>
          </details>
        ))}
      </div>
    </section>
  );
}