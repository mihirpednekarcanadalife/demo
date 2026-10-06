import { useCallback, useEffect, useMemo, useState } from "react";
import { fetchCases } from "../api/caseApi";
import CaseTable from "../components/CaseTable";

const ALL = "ALL";

export default function CaseWorkflowPage() {
  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [statusFilter, setStatusFilter] = useState(ALL);
  const [search, setSearch] = useState("");
  const [lastRefreshed, setLastRefreshed] = useState(null);

  const loadCases = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const data = await fetchCases();
      setCases(Array.isArray(data) ? data : []);
      setLastRefreshed(new Date());
    } catch (e) {
      setError(e.message || "Failed to load cases");
      setCases([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCases();
  }, [loadCases]);

  const statuses = useMemo(() => {
    const unique = new Set(cases.map((item) => item.caseStatus).filter(Boolean));
    return [ALL, ...Array.from(unique).sort()];
  }, [cases]);

  const visibleCases = useMemo(() => {
    const term = search.trim().toLowerCase();

    return cases.filter((item) => {
      const matchesStatus = statusFilter === ALL || item.caseStatus === statusFilter;
      if (!matchesStatus) return false;
      if (!term) return true;

      return [item.caseId, item.caseName, item.owner, item.policyId, item.email]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(term));
    });
  }, [cases, statusFilter, search]);

  return (
    <section>
      <h1>Case workflow</h1>
      <p>
        All cases raised by the retirement journey batch job, with their current workflow status and
        routing details.
      </p>

      <div className="card">
        <div className="toolbar">
          <label htmlFor="case-search">
            Search
            <input
              id="case-search"
              type="search"
              placeholder="Case ID, name, owner, policy or email"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />
          </label>

          <label htmlFor="case-status">
            Status
            <select
              id="case-status"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              {statuses.map((status) => (
                <option key={status} value={status}>
                  {status === ALL ? "All statuses" : status}
                </option>
              ))}
            </select>
          </label>

          <button type="button" onClick={loadCases} disabled={loading}>
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </div>

        <p className="muted">
          Showing {visibleCases.length} of {cases.length} case{cases.length === 1 ? "" : "s"}
          {lastRefreshed ? ` - last refreshed ${lastRefreshed.toLocaleTimeString()}` : ""}
        </p>
      </div>

      {error && <p className="error">{error}</p>}

      {loading && cases.length === 0 ? (
        <p className="muted">Loading cases...</p>
      ) : (
        <div className="card">
          <CaseTable cases={visibleCases} />
        </div>
      )}
    </section>
  );
}