export default function EmailViewPage({ caseItem, onBack, onGoToPortal, sending, sendError }) {
  const recipient = caseItem.email || "customer@example.com";
  const ownerLabel = caseItem.owner || "Customer";

  return (
    <section>
      <button type="button" className="link-button" onClick={onBack}>
        &larr; Back to email list
      </button>

      <h1>Maturity welcome email</h1>
      <p>
        Preview of the welcome package issued for case <strong>{caseItem.caseId}</strong>.
      </p>

      {sending && <p className="muted">Updating case status...</p>}
      {sendError && <p className="error">{sendError}</p>}
      {!sending && !sendError && (
        <p className="success">Case status updated to MATURITY_PACKAGE_SENT.</p>
      )}

      <div className="card email-preview">
        <dl className="email-headers">
          <div>
            <dt>To</dt>
            <dd>{recipient}</dd>
          </div>
          <div>
            <dt>Subject</dt>
            <dd>Your retirement options - policy {caseItem.policyId || "-"}</dd>
          </div>
          <div>
            <dt>Case</dt>
            <dd>
              {caseItem.caseId} ({caseItem.ownerType || "-"})
            </dd>
          </div>
        </dl>

        <hr />

        <div className="email-body">
          <p>Dear {ownerLabel},</p>

          <p>
            Welcome to the next step of your retirement journey. Our records show that your pension
            policy <strong>{caseItem.policyId || "-"}</strong> is approaching its maturity date, so
            we have prepared your retirement options package.
          </p>

          <p>In your customer portal you can:</p>
          <ul>
            <li>review the current value and projection of your pension fund</li>
            <li>see the retirement options available to you</li>
            <li>track the progress of your case</li>
          </ul>

          <p>
            <button type="button" className="email-cta" onClick={onGoToPortal}>
              Go to the customer portal
            </button>
          </p>

          <p className="muted">
            You will be asked to sign in with your username and password to continue.
          </p>

          <p>
            Kind regards,
            <br />
            The Retirement Team
          </p>
        </div>
      </div>
    </section>
  );
}