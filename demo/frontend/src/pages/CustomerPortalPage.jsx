import { useCallback, useEffect, useState } from "react";
import { fetchCases } from "../api/caseApi";
import { MOCK_CREDENTIALS_HINT, signIn } from "../auth/mockAuth";
import CaseJourneyPanel from "../components/CaseJourneyPanel";
import PensionProjectionPage from "./PensionProjectionPage";

function statusClass(status) {
  if (!status) return "badge";
  return `badge badge-${status.toLowerCase().replace(/_/g, "-")}`;
}

function formatInstant(value) {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function LoginForm({ onSignIn, error }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  function handleSubmit(event) {
    event.preventDefault();
    onSignIn(username, password);
  }

  return (
    <form className="card login-card" onSubmit={handleSubmit}>
      <h2>Sign in</h2>
      <p className="muted">Your username is your adviser/owner id. {MOCK_CREDENTIALS_HINT}</p>

      <label htmlFor="portal-username">
        Username
        <input
          id="portal-username"
          name="username"
          autoComplete="username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
        />
      </label>

      <label htmlFor="portal-password">
        Password
        <input
          id="portal-password"
          name="password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />
      </label>

      {error && <p className="error">{error}</p>}

      <button type="submit">Sign in</button>
    </form>
  );
}

export default function CustomerPortalPage() {
  const [user, setUser] = useState(null);
  const [authError, setAuthError] = useState("");

  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [selected, setSelected] = useState(null);

  const loadCases = useCallback(async (owner) => {
    try {
      setLoading(true);
      setLoadError("");
      const data = await fetchCases();
      const mine = (Array.isArray(data) ? data : []).filter(
        (item) => (item.owner || "").toLowerCase() === owner.toLowerCase()
      );
      setCases(mine);
    } catch (e) {
      setLoadError(e.message || "Failed to load your cases");
      setCases([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (user) {
      loadCases(user.username);
    }
  }, [user, loadCases]);

  function handleSignIn(username, password) {
    try {
      setAuthError("");
      setUser(signIn(username, password));
    } catch (e) {
      setAuthError(e.message);
    }
  }

  function handleSignOut() {
    setUser(null);
    setCases([]);
    setSelected(null);
    setAuthError("");
  }

  if (!user) {
    return (
      <section>
        <h1>Customer portal</h1>
        <p>Sign in to see the cases and pension policies registered to you.</p>
        <LoginForm onSignIn={handleSignIn} error={authError} />
      </section>
    );
  }

  if (selected) {
    return (
      <PensionProjectionPage
        policyId={selected.policyId}
        onBack={() => {
          setSelected(null);
          loadCases(user.username);
        }}
      >
        <CaseJourneyPanel
          caseId={selected.caseId}
          onChanged={() => loadCases(user.username)}
        />
      </PensionProjectionPage>
    );
  }

  return (
    <section>
      <div className="portal-header">
        <div>
          <h1>Welcome, {user.username}</h1>
          <p>
            Cases registered to your account. Select a policy to view its fund projection and
            complete your retirement options.
          </p>
        </div>
        <button type="button" onClick={handleSignOut}>
          Sign out
        </button>
      </div>

      {loadError && <p className="error">{loadError}</p>}

      <div className="card">
        <p className="muted">
          {loading
            ? "Loading your cases..."
            : `${cases.length} case${cases.length === 1 ? "" : "s"} found`}
        </p>

        {!loading && cases.length === 0 && !loadError && (
          <p className="muted">No cases are registered to this username.</p>
        )}

        {cases.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th scope="col">Case ID</th>
                  <th scope="col">Case name</th>
                  <th scope="col">Status</th>
                  <th scope="col">SLA</th>
                  <th scope="col">Policy ID</th>
                  <th scope="col">Email</th>
                  <th scope="col">Updated at</th>
                </tr>
              </thead>
              <tbody>
                {cases.map((item) => (
                  <tr key={item.caseId}>
                    <td>{item.caseId}</td>
                    <td>{item.caseName}</td>
                    <td>
                      <span className={statusClass(item.caseStatus)}>{item.caseStatus}</span>
                    </td>
                    <td>{item.caseSla || "-"}</td>
                    <td>
                      {item.policyId ? (
                        <a
                          href={`#policy-${item.policyId}`}
                          className="policy-link"
                          onClick={(event) => {
                            event.preventDefault();
                            setSelected({ policyId: item.policyId, caseId: item.caseId });
                          }}
                        >
                          {item.policyId}
                        </a>
                      ) : (
                        <span className="muted">-</span>
                      )}
                    </td>
                    <td>{item.email || <span className="muted">-</span>}</td>
                    <td>{formatInstant(item.updatedAt)}</td>
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