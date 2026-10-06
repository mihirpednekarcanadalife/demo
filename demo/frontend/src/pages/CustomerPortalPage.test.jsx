import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import CustomerPortalPage from "./CustomerPortalPage";

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
    owner: "someone-else",
    ownerType: "CLE",
    email: "cle@cle.com",
    policyId: "POL-2002",
    updatedAt: "2026-10-05T10:20:00.000Z"
  }
];

function mockFetch() {
  return vi.fn().mockImplementation((url) => {
    if (String(url).includes("/policies/")) {
      return Promise.resolve({
        ok: true,
        json: async () => ({ policyId: "POL-1001", maturityDate: "2030-06-15" })
      });
    }
    return Promise.resolve({ ok: true, json: async () => CASES });
  });
}

async function signInAs(username, password = "pension123") {
  fireEvent.change(screen.getByLabelText(/username/i), { target: { value: username } });
  fireEvent.change(screen.getByLabelText(/password/i), { target: { value: password } });
  fireEvent.click(screen.getByRole("button", { name: /sign in/i }));
}

describe("CustomerPortalPage", () => {
  beforeEach(() => {
    global.fetch = mockFetch();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("rejects a wrong password", async () => {
    render(<CustomerPortalPage />);

    await signInAs("advisor-7", "wrong");

    expect(await screen.findByText(/invalid username or password/i)).toBeInTheDocument();
    expect(global.fetch).not.toHaveBeenCalled();
  });

  it("shows only the cases owned by the signed-in username", async () => {
    render(<CustomerPortalPage />);

    await signInAs("advisor-7");

    await waitFor(() => expect(screen.getByText("CASE-0001")).toBeInTheDocument());

    expect(screen.getByText(/welcome, advisor-7/i)).toBeInTheDocument();
    expect(screen.queryByText("CASE-0002")).not.toBeInTheDocument();
    expect(screen.getByText("1 case found")).toBeInTheDocument();
  });

  it("opens the pension projection page from the policy hyperlink", async () => {
    render(<CustomerPortalPage />);

    await signInAs("advisor-7");

    const link = await screen.findByRole("link", { name: "POL-1001" });
    fireEvent.click(link);

    expect(await screen.findByText(/pension fund projection/i)).toBeInTheDocument();
    expect(screen.getByText(/projected fund value/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /back to my cases/i }));
    await waitFor(() => expect(screen.getByText("CASE-0001")).toBeInTheDocument());
  });

  it("signs the user out", async () => {
    render(<CustomerPortalPage />);

    await signInAs("advisor-7");
    await waitFor(() => expect(screen.getByText("CASE-0001")).toBeInTheDocument());

    fireEvent.click(screen.getByRole("button", { name: /sign out/i }));

    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
  });
});