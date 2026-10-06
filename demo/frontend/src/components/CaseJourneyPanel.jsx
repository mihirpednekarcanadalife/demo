import { useCallback, useEffect, useState } from "react";
import { fetchCaseJourney, selectMaturityOption, uploadDocument } from "../api/caseApi";

const OPTION_LABELS = {
  ANNUITY: "Annuity",
  LUMP_SUM: "Lump sum",
  REINVEST: "Reinvest"
};

const DOCUMENT_LABELS = {
  PASSPORT: "Passport",
  BANK_DETAILS: "Bank details"
};

function labelFor(map, value) {
  return map[value] || value;
}

export default function CaseJourneyPanel({ caseId, onChanged }) {
  const [journey, setJourney] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [choice, setChoice] = useState("");

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const data = await fetchCaseJourney(caseId);
      setJourney(data);
      setChoice(data.retirementCase.maturityOption || "");
    } catch (e) {
      setError(e.message || "Failed to load your case");
    } finally {
      setLoading(false);
    }
  }, [caseId]);

  useEffect(() => {
    load();
  }, [load]);

  function applyResult(result) {
    setJourney(result);
    setChoice(result.retirementCase.maturityOption || "");
    onChanged?.();
  }

  async function handleSubmitOption(event) {
    event.preventDefault();
    if (!choice) {
      setError("Please choose a maturity option");
      return;
    }
    try {
      setBusy(true);
      setError("");
      applyResult(await selectMaturityOption(caseId, choice));
    } catch (e) {
      setError(e.message || "Failed to submit your selection");
    } finally {
      setBusy(false);
    }
  }

  async function handleUpload(documentType) {
    try {
      setBusy(true);
      setError("");
      applyResult(await uploadDocument(caseId, documentType, `${documentType.toLowerCase()}.pdf`));
    } catch (e) {
      setError(e.message || "Failed to upload the document");
    } finally {
      setBusy(false);
    }
  }

  if (loading) {
    return <p className="muted">Loading your case...</p>;
  }

  if (!journey) {
    return <p className="error">{error || "No case found for this policy"}</p>;
  }

  const { retirementCase, availableOptions, requiredDocuments, uploadedDocuments } = journey;
  const status = retirementCase.caseStatus;

  return (
    <>
      <h2>Your retirement options</h2>
      <p>
        Case <strong>{retirementCase.caseId}</strong> -{" "}
        <span className={`badge badge-${status.toLowerCase().replace(/_/g, "-")}`}>{status}</span>
      </p>

      {error && <p className="error">{error}</p>}

      {/* Stage 3 - final state */}
      {status === "COMPLETED" ? (
        <div className="card">
          <h3>You are ready for retirement</h3>
          <p className="success">{journey.message}</p>

          <div className="grid">
            <div className="stat">
              <span className="stat-label">Selected option</span>
              <strong>{labelFor(OPTION_LABELS, retirementCase.maturityOption)}</strong>
            </div>
            <div className="stat">
              <span className="stat-label">Documents accepted</span>
              <strong>
                {uploadedDocuments.length} of {requiredDocuments.length}
              </strong>
            </div>
            <div className="stat">
              <span className="stat-label">Case status</span>
              <strong>{status}</strong>
            </div>
          </div>

          <h3>Uploaded documents</h3>
          <ul className="document-list">
            {uploadedDocuments.map((document) => (
              <li key={document}>
                <span>{labelFor(DOCUMENT_LABELS, document)}</span>
                <span className="badge badge-completed">Accepted</span>
              </li>
            ))}
          </ul>
        </div>
      ) : status === "AWAITING_INFORMATION" ? (
        /* Stage 2 - missing documents, all required before completing */
        <div className="card">
          <h3>Missing documents</h3>
          <p>
            You selected <strong>{labelFor(OPTION_LABELS, retirementCase.maturityOption)}</strong>.
            Upload <strong>all</strong> of the documents below to finalise your case.
          </p>
          <p className="muted">
            {uploadedDocuments.length} of {requiredDocuments.length} received.
          </p>

          <ul className="document-list">
            {requiredDocuments.map((document) => {
              const uploaded = uploadedDocuments.includes(document);
              return (
                <li key={document}>
                  <span>{labelFor(DOCUMENT_LABELS, document)}</span>
                  {uploaded ? (
                    <span className="badge badge-completed">Accepted</span>
                  ) : (
                    <button type="button" disabled={busy} onClick={() => handleUpload(document)}>
                      Upload
                    </button>
                  )}
                </li>
              );
            })}
          </ul>
        </div>
      ) : (
        /* Stage 1 - choose an option */
        <form className="card" onSubmit={handleSubmitOption}>
          <h3>Choose your maturity option</h3>
          <p>{journey.message}</p>

          {availableOptions.map((option) => (
            <label key={option} className="product-option">
              <input
                type="radio"
                name="maturityOption"
                value={option}
                checked={choice === option}
                onChange={(event) => setChoice(event.target.value)}
              />
              <span className="product-option-text">{labelFor(OPTION_LABELS, option)}</span>
            </label>
          ))}

          <p>
            <button type="submit" disabled={busy}>
              {busy ? "Submitting..." : "Submit selection"}
            </button>
          </p>
        </form>
      )}
    </>
  );
}