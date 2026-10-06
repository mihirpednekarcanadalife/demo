export default function ComparisonResults({ result }) {
  if (!result) {
    return null;
  }

  return (
    <div className="card">
      <h2>Comparison Results</h2>
      <p className="success">Best Product: {result.bestProductId}</p>
      <div className="table-wrap">
        <table>
        <thead>
          <tr>
            <th>Rank</th>
            <th>Product</th>
            <th>Provider</th>
            <th>Total Score</th>
            <th>Fee Score</th>
            <th>Credit Score</th>
            <th>Flexibility</th>
            <th>Digital</th>
            <th>Projected Fee Cost (EUR)</th>
            <th>Projected Fund at Retirement (EUR)</th>
          </tr>
        </thead>
        <tbody>
          {result.ranking.map((item, index) => (
            <tr key={item.productId}>
              <td>{index + 1}</td>
              <td>{item.productName}</td>
              <td>{item.provider}</td>
              <td>{item.totalScore}</td>
              <td>{item.feeScore}</td>
              <td>{item.creditRatingScore}</td>
              <td>{item.flexibilityScore}</td>
              <td>{item.digitalServicesScore}</td>
              <td>{item.projectedFeeCostUntilRetirementEuros}</td>
              <td>{item.projectedFundValueAtRetirementEuros}</td>
            </tr>
          ))}
        </tbody>
        </table>
      </div>
    </div>
  );
}