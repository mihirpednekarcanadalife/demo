import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import EmailPage from "./EmailPage";

const CASES = [
  {
    caseId: "CASE-0001",
    caseName: "Maturity detected for policy POL-1001",
    caseStatus: "MATURITY_DETECTED",
    caseSla: "30d",
    owner: "advisor-7",
    ownerType: null,
    email: null,
    policyId: "POL-1001",
    updatedAt: "2026-10-05T09:14:32.481Z"
  },
  {
    caseId: "CASE-0002",
    caseName: "Maturity detected for policy POL-2002",
    caseStatus: "CLE_OWNER_DETECTED",
    caseSla: "30d",
    owner: "CLE",
    ownerType: "CLE",
    email: "cle@cle.com",
    policyId: "POL-2002",
    updatedAt: "2026-10-05T10:20:00.000Z"
  },
  {
    caseId: "CASE-0003",
    caseName: "Maturity detected for policy POL-3003",
    caseStatus: "NON_CLE_OWNER_DETECTED",
    caseSla: "30d",
    owner: "advisor-42",
    ownerType: "NON_CLE",
    email: "non-cle@cle.com",
    policyId: "POL-3003",
    updatedAt: "2026-10-05T11:00:00.000Z"
  },
  {
    caseId: "CASE-0004",
    caseName: "Closed case",
    caseStatus: "COMPLETED",
    caseSla: "30d",
    owner: "advisor-9",
    ownerType: "NON_CLE",
    email: "non-cle@cle.com",
    policyId: "POL-4004",
    updatedAt: "2026-10-05T12:00:00.000Z"
  }
];

describe("EmailPage", () => {
  beforeEach(() => {
    global.fetch = vi.fn().mockImplementation((url, options) => {
      if (options?.method === "PUT") {
        return Promise.resolve({
          ok: true,
          json: async () => ({ ...CASES[1], caseStatus: "MATURITY_PACKAGE_SENT" })
        });
      }
      return Promise.resolve({ ok: true, json: async () => CASES });
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("shows only CLE_OWNER_DETECTED and NON_CLE_OWNER_DETECTED cases", async () => {
    render(<EmailPage />);

    await waitFor(() => expect(screen.getByText("CASE-0002")).toBeInTheDocument());

    expect(global.fetch).toHaveBeenCalledWith("http://localhost:8080/demo/api/v1/cases");
    expect(screen.getByText("CASE-0003")).toBeInTheDocument();

    // Filtered out: not owner-detected.
    expect(screen.queryByText("CASE-0001")).not.toBeInTheDocument();
    expect(screen.queryByText("CASE-0004")).not.toBeInTheDocument();
  });

  it("summarises the CLE and NON-CLE counts", async () => {
    render(<EmailPage />);

    await waitFor(() => expect(screen.getByText("CASE-0002")).toBeInTheDocument());

    expect(screen.getByText("CLE owner detected")).toBeInTheDocument();
    expect(screen.getByText("NON-CLE owner detected")).toBeInTheDocument();
    expect(screen.getByText("Maturity package sent")).toBeInTheDocument();
  });

  it("filters by a single owner-detected status", async () => {
    render(<EmailPage />);

    await waitFor(() => expect(screen.getByText("CASE-0002")).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText(/status/i), {
      target: { value: "NON_CLE_OWNER_DETECTED" }
    });

    expect(screen.queryByText("CASE-0002")).not.toBeInTheDocument();
    expect(screen.getByText("CASE-0003")).toBeInTheDocument();
  });

  it("renders a View link per case", async () => {
    render(<EmailPage />);

    await waitFor(() => expect(screen.getByText("CASE-0002")).toBeInTheDocument());

    expect(screen.getAllByRole("link", { name: "View" })).toHaveLength(2);
  });

  it("opens the welcome email and marks the package as sent", async () => {
    render(<EmailPage />);

    await waitFor(() => expect(screen.getByText("CASE-0002")).toBeInTheDocument());

    fireEvent.click(screen.getAllByRole("link", { name: "View" })[0]);

    expect(await screen.findByText(/maturity welcome email/i)).toBeInTheDocument();
    expect(screen.getByText(/welcome to the next step of your retirement journey/i)).toBeInTheDocument();

    await waitFor(() =>
      expect(
        global.fetch
      ).toHaveBeenCalledWith(
        "http://localhost:8080/demo/api/v1/cases/CASE-0002",
        expect.objectContaining({
          method: "PUT",
          body: JSON.stringify({ caseStatus: "MATURITY_PACKAGE_SENT" })
        })
      )
    );

    expect(
      await screen.findByText(/case status updated to maturity_package_sent/i)
    ).toBeInTheDocument();
  });

  it("navigates to the customer portal from the email link", async () => {
    const onNavigate = vi.fn();
    render(<EmailPage onNavigate={onNavigate} />);

    await waitFor(() => expect(screen.getByText("CASE-0002")).toBeInTheDocument());
    fireEvent.click(screen.getAllByRole("link", { name: "View" })[0]);

    fireEvent.click(await screen.findByRole("button", { name: /go to the customer portal/i }));

    expect(onNavigate).toHaveBeenCalledWith("portal");
  });

  it("shows an empty state when nothing is owner-detected", async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: async () => [CASES[0]] });

    render(<EmailPage />);

    await waitFor(() =>
      expect(screen.getByText("No owner-detected cases found.")).toBeInTheDocument()
    );
  });

  it("shows an error when the API fails", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({ message: "Boom" })
    });

    render(<EmailPage />);

    await waitFor(() => expect(screen.getByText("Boom")).toBeInTheDocument());
  });
});