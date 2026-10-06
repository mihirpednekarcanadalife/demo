import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import CaseWorkflowPage from "./CaseWorkflowPage";

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
    description: "Policy POL-1001 matures soon.",
    createdAt: "2026-10-05T09:14:32.481Z",
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
    description: "Policy POL-2002 matures soon.",
    createdAt: "2026-10-05T10:14:32.481Z",
    updatedAt: "2026-10-05T10:20:00.000Z"
  }
];

describe("CaseWorkflowPage", () => {
  beforeEach(() => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => CASES
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("fetches cases from the cases API and renders every field as a column", async () => {
    render(<CaseWorkflowPage />);

    await waitFor(() => expect(screen.getByText("CASE-0001")).toBeInTheDocument());

    expect(global.fetch).toHaveBeenCalledWith("http://localhost:8080/demo/api/v1/cases");

    const headers = screen.getAllByRole("columnheader").map((cell) => cell.textContent);
    expect(headers).toEqual([
      "Case ID",
      "Case name",
      "Status",
      "SLA",
      "Owner",
      "Owner type",
      "Email",
      "Policy ID",
      "Description",
      "Created at",
      "Updated at"
    ]);

    expect(screen.getByText("CASE-0002")).toBeInTheDocument();
    expect(screen.getByText("cle@cle.com")).toBeInTheDocument();
    expect(screen.getByText("POL-1001")).toBeInTheDocument();
  });

  it("filters the table by status", async () => {
    render(<CaseWorkflowPage />);

    await waitFor(() => expect(screen.getByText("CASE-0001")).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText(/status/i), { target: { value: "CLE_OWNER_DETECTED" } });

    expect(screen.queryByText("CASE-0001")).not.toBeInTheDocument();
    expect(screen.getByText("CASE-0002")).toBeInTheDocument();
  });

  it("filters the table by free text search", async () => {
    render(<CaseWorkflowPage />);

    await waitFor(() => expect(screen.getByText("CASE-0001")).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText(/search/i), { target: { value: "POL-2002" } });

    expect(screen.queryByText("CASE-0001")).not.toBeInTheDocument();
    expect(screen.getByText("CASE-0002")).toBeInTheDocument();
  });

  it("shows an error message when the API fails", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({ message: "Boom" })
    });

    render(<CaseWorkflowPage />);

    await waitFor(() => expect(screen.getByText("Boom")).toBeInTheDocument());
    expect(screen.getByText("No cases found.")).toBeInTheDocument();
  });

  it("refetches when Refresh is clicked", async () => {
    render(<CaseWorkflowPage />);

    await waitFor(() => expect(screen.getByText("CASE-0001")).toBeInTheDocument());

    fireEvent.click(screen.getByRole("button", { name: /refresh/i }));

    await waitFor(() => expect(global.fetch).toHaveBeenCalledTimes(2));
  });
});