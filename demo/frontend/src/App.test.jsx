import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import App from "./App";

vi.mock("./api/pensionApi", () => ({
  fetchProducts: vi.fn(async () => [
    { id: "P1", name: "Plan A", provider: "InsureOne", annualFeePercent: 0.6 },
    { id: "P2", name: "Plan B", provider: "InsureTwo", annualFeePercent: 0.8 }
  ]),
  compareProducts: vi.fn(async () => ({
    bestProductId: "P1",
    ranking: [
      {
        productId: "P1",
        productName: "Plan A",
        provider: "InsureOne",
        totalScore: 0.9,
        feeScore: 1,
        creditRatingScore: 0.9,
        flexibilityScore: 0.8,
        digitalServicesScore: 0.7,
        projectedFeeCostUntilRetirementEuros: 10000,
        projectedFundValueAtRetirementEuros: 190000
      }
    ]
  }))
}));

describe("App", () => {
  it("loads products and shows comparison result", async () => {
    render(<App />);

    await waitFor(() => {
      expect(screen.getByText(/Plan A/)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByLabelText(/Plan A/));
    fireEvent.click(screen.getByLabelText(/Plan B/));
    fireEvent.click(screen.getByRole("button", { name: /Compare Products/i }));

    await waitFor(() => {
      expect(screen.getByText(/Best Product: P1/)).toBeInTheDocument();
    });
  });
});