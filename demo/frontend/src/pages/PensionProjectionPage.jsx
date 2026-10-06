import { useEffect, useState } from "react";
import { fetchPolicy } from "../api/policyApi";
import { buildMockProjection, formatEuros } from "../mock/pensionProjection";

export default function PensionProjectionPage({ policyId, onBack, children }) {
  const [policy, setPolicy] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState("");

  useEffect(() => {
    let active = true;
    setLoading(true);

    fetchPolicy(policyId)
      .then((data) => {
        if (active) {
          setPolicy(data);
          setNotice("");
        }
      })
      .catch(() => {
        if (active) {
          setPolicy(null);
          setNotice("Live policy details unavailable - showing illustrative figures only.");
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [policyId]);

  const projection = buildMockProjection(policyId, policy);

  return (
    <section>
      <button type="button" className="link-button" onClick={onBack}>
        &larr; Back to my cases
      </button>

      <h1>Your policy {policyId}</h1>
      <p>
        Illustrative projection for policy <strong>{policyId}</strong>. Figures are mock values for
        demonstration and are not a statement of benefits.
      </p>

      {loading && <p className="muted">Loading policy details...</p>}
      {notice && <p className="muted">{notice}</p>}

      {/* Case actions: maturity options, missing documents and the final state. */}
      {children}

      <h2>Fund projection</h2>

      <div className="card">
        <h3>Today</h3>
        <div className="grid">
          <div className="stat">
            <span className="stat-label">Current fund value</span>
            <strong>{formatEuros(projection.currentFundValueEuros)}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Monthly contribution</span>
            <strong>{formatEuros(projection.monthlyContributionEuros)}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Assumed growth (p.a.)</span>
            <strong>{projection.annualGrowthRatePercent.toFixed(1)}%</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Management fee (p.a.)</span>
            <strong>{projection.annualManagementFeePercent.toFixed(2)}%</strong>
          </div>
        </div>
      </div>

      <div className="card">
        <h3>At retirement</h3>
        <div className="grid">
          <div className="stat">
            <span className="stat-label">Years to maturity</span>
            <strong>{projection.yearsToMaturity}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Maturity date</span>
            <strong>{projection.maturityDate ?? "Not available"}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Projected fund value</span>
            <strong>{formatEuros(projection.projectedValueAtMaturityEuros)}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Tax-free lump sum (25%)</span>
            <strong>{formatEuros(projection.taxFreeLumpSumEuros)}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Estimated income (p.a.)</span>
            <strong>{formatEuros(projection.estimatedAnnualIncomeEuros)}</strong>
          </div>
          <div className="stat">
            <span className="stat-label">Total fees paid</span>
            <strong>{formatEuros(projection.totalFeesEuros)}</strong>
          </div>
        </div>
      </div>

      <div className="card">
        <h3>Year by year</h3>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th scope="col">Year</th>
                <th scope="col">Opening balance</th>
                <th scope="col">Contributions</th>
                <th scope="col">Growth</th>
                <th scope="col">Fees</th>
                <th scope="col">Closing balance</th>
              </tr>
            </thead>
            <tbody>
              {projection.yearlyProjection.map((row) => (
                <tr key={row.year}>
                  <td>{row.year}</td>
                  <td>{formatEuros(row.openingBalanceEuros)}</td>
                  <td>{formatEuros(row.contributionsEuros)}</td>
                  <td>{formatEuros(row.growthEuros)}</td>
                  <td>-{formatEuros(row.feesEuros)}</td>
                  <td>
                    <strong>{formatEuros(row.closingBalanceEuros)}</strong>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}