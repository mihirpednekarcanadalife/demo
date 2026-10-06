const COLUMNS = [
  { key: "caseId", label: "Case ID" },
  { key: "caseName", label: "Case name" },
  { key: "caseStatus", label: "Status" },
  { key: "caseSla", label: "SLA" },
  { key: "owner", label: "Owner" },
  { key: "ownerType", label: "Owner type" },
  { key: "email", label: "Email" },
  { key: "policyId", label: "Policy ID" },
  { key: "description", label: "Description" },
  { key: "createdAt", label: "Created at" },
  { key: "updatedAt", label: "Updated at" }
];

function statusClass(status) {
  if (!status) return "badge";
  return `badge badge-${status.toLowerCase().replace(/_/g, "-")}`;
}

function formatInstant(value) {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function renderCell(row, key) {
  const value = row[key];

  if (value === null || value === undefined || value === "") {
    return <span className="muted">-</span>;
  }
  if (key === "caseStatus") {
    return <span className={statusClass(value)}>{value}</span>;
  }
  if (key === "createdAt" || key === "updatedAt") {
    return formatInstant(value);
  }
  if (key === "email") {
    return <a href={`mailto:${value}`}>{value}</a>;
  }
  return value;
}

export default function CaseTable({ cases }) {
  if (!cases.length) {
    return <p className="muted">No cases found.</p>;
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {COLUMNS.map((column) => (
              <th key={column.key} scope="col">
                {column.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {cases.map((row) => (
            <tr key={row.caseId}>
              {COLUMNS.map((column) => (
                <td key={column.key} data-label={column.label}>
                  {renderCell(row, column.key)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export { COLUMNS };