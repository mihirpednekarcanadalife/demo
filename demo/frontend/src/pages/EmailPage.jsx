import { useCallback, useEffect, useMemo, useState } from "react";
import { fetchCases, updateCaseStatus } from "../api/caseApi";
import EmailViewPage from "./EmailViewPage";

/** Only cases that have completed owner detection are ready for email routing. */
export const EMAIL_READY_STATUSES = ["CLE_OWNER_DETECTED", "NON_CLE_OWNER_DETECTED"];

/** Shown alongside the ready statuses so sent packages remain visible. */
export const PACKAGE_SENT_STATUS = "MATURITY_PACKAGE_SENT";

const VISIBLE_STATUSES = [...EMAIL_READY_STATUSES, PACKAGE_SENT_STATUS];

const ALL = "ALL";

function statusClass(status) {
  if (!status) return "badge";
  return `badge badge-${status.toLowerCase().replace(/_/g, "-")}`;
}

function formatInstant(value) {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function mailtoHref(item) {
  const subject = `Retirement maturity - case ${item.caseId}`;
  return `mailto:${item.email}?subject=${encodeURIComponent(subject)}`;
}

export default function EmailPage({ onNavigate }) {
  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [statusFilter, setStatusFilter] = useState(ALL);

  const [openedCase, setOpenedCase] = useState(null);
  const [sending, setSending] = useState(false);
  const [sendError, setSendError] = useState("");

  const loadCases = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const data = await fetchCases();
      const emailReady = (Array.isArray(data) ? data : []).filter((item) =>
        VISIBLE_STATUSES.includes(item.caseStatus)
      );
      setCases(emailReady);
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

  /** Opening the email marks the maturity package as sent. */
  const handleView = useCallback(async (item) => {
    setOpenedCase(item);
    setSendError("");

    if (item.caseStatus === PACKAGE_SENT_STATUS) {
      return;
    }

    try {
      setSending(true);
      const updated = await updateCaseStatus(item.caseId, PACKAGE_SENT_STATUS);
      setOpenedCase(updated);
      setCases((previous) =>
        previous.map((row) => (row.caseId === updated.caseId ? updated : row))
      );
    } catch (e) {
      setSendError(e.message || "Failed to update the case status");
    } finally {
      setSending(false);
    }
  }, []);

  const counts = useMemo(
    () => ({
      cle: cases.filter((item) => item.caseStatus === "CLE_OWNER_DETECTED").length,
      nonCle: cases.filter((item) => item.caseStatus === "NON_CLE_OWNER_DETECTED").length,
      sent: cases.filter((item) => item.caseStatus === PACKAGE_SENT_STATUS).length
    }),
    [cases]
  );

  const visibleCases = useMemo(
    () => (statusFilter === ALL ? cases : cases.filter((item) => item.caseStatus === statusFilter)),
    [cases, statusFilter]
  );

  if (openedCase) {
    return (
      <EmailViewPage
        caseItem={openedCase}
        sending={sending}
        sendError={sendError}
        onBack={() => {
          setOpenedCase(null);
          loadCases();
        }}
        onGoToPortal={() => onNavigate?.("portal")}
      />
    );
  }

  return (
    <section>
      <h1>Email</h1>
      <p>
        Cases that have completed owner detection and are ready for email routing to the CLE or
        NON-CLE mailbox.
      </p>

      <div className="card">
        <div className="grid">
          <div className="stat">
            <span className="stat-label">CLE owner detected</span>
            <strong>{counts.cle}</strong>
            <span className="muted">cle@cle.com</span>
          </div>
          <div className="stat">
            <span className="stat-label">NON-CLE owner detected</span>
            <strong>{counts.nonCle}</strong>
            <span className="muted">non-cle@cle.com</span>
          </div>
          <div className="stat">
            <span className="stat-label">Maturity package sent</span>
            <strong>{counts.sent}</strong>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="toolbar">
          <label htmlFor="email-status">
            Status
            <select
              id="email-status"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value={ALL}>All</option>
              {VISIBLE_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </label>

          <button type="button" onClick={loadCases} disabled={loading}>
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </div>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="card">
        {loading && cases.length === 0 ? (
          <p className="muted">Loading cases...</p>
        ) : visibleCases.length === 0 ? (
          <p className="muted">No owner-detected cases found.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th scope="col">Case ID</th>
                  <th scope="col">Case name</th>
                  <th scope="col">Status</th>
                  <th scope="col">Owner</th>
                  <th scope="col">Owner type</th>
                  <th scope="col">Email</th>
                  <th scope="col">Policy ID</th>
                  <th scope="col">SLA</th>
                  <th scope="col">Updated at</th>
                  <th scope="col">Action</th>
                </tr>
              </thead>
              <tbody>
                {visibleCases.map((item) => (
                  <tr key={item.caseId}>
                    <td>{item.caseId}</td>
                    <td>{item.caseName}</td>
                    <td>
                      <span className={statusClass(item.caseStatus)}>{item.caseStatus}</span>
                    </td>
                    <td>{item.owner || <span className="muted">-</span>}</td>
                    <td>{item.ownerType || <span className="muted">-</span>}</td>
                    <td>
                      {item.email ? (
                        <a href={mailtoHref(item)}>{item.email}</a>
                      ) : (
                        <span className="muted">-</span>
                      )}
                    </td>
                    <td>{item.policyId || <span className="muted">-</span>}</td>
                    <td>{item.caseSla || <span className="muted">-</span>}</td>
                    <td>{formatInstant(item.updatedAt)}</td>
                    <td>
                      <a
                        href={`#email-${item.caseId}`}
                        className="policy-link"
                        onClick={(event) => {
                          event.preventDefault();
                          handleView(item);
                        }}
                      >
                        View
                      </a>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}