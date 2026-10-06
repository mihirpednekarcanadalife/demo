import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import RetirementJourneyPage from "./RetirementJourneyPage";

const plan = {
  planId: "plan-1",
  householdName: "Murphy household",
  yearsToRetirement: 1,
  derivedRiskProfile: "CONSERVATIVE",
  planSummary: "Murphy household is 1 year from retirement.",
  planConfidence: 0.89,
  incomeProjection: {
    combinedPensionPotEuros: 760000,
    taxFreeLumpSumEuros: 190000,
    reinvestablePotEuros: 570000,
    sustainableAnnualDrawdownEuros: 22800,
    statePensionAnnualEuros: 28000,
    totalProjectedAnnualIncomeEuros: 50800,
    targetAnnualIncomeEuros: 55000,
    annualIncomeGapEuros: 4200,
    targetCoveragePercent: 92.4,
    potLongevityYears: 30
  },
  reinvestmentAllocation: [
    {
      bucket: "Liquidity",
      vehicle: "Cash & short-term deposits",
      allocationPercent: 12.3,
      amountEuros: 70000,
      purpose: "Funds the next 2 years of income.",
      timeHorizon: "0-2 years"
    }
  ],
  journey: [
    {
      sequence: 1,
      timeframe: "12 months before retirement",
      title: "Confirm the retirement date and baseline",
      description: "Agree the retirement date.",
      ownerAgent: "household-profile-agent",
      actions: ["Collect all pension statements"]
    }
  ],
  guardrails: ["Projections are illustrative only."],
  nextBestActions: ["Request up-to-date benefit statements"],
  agentTrace: [
    {
      agentId: "household-profile-agent",
      agentName: "Household Profile Agent",
      role: "Normalises partner data.",
      order: 1,
      headline: "Household is 1 year from retirement.",
      findings: ["Combined pot EUR 760,000."],
      recommendedActions: ["Collect statements"],
      confidence: 0.95
    }
  ]
};

const agents = [{ id: "household-profile-agent", name: "Household Profile Agent", role: "Profiles the household", order: 1 }];

vi.mock("../api/retirementApi", () => ({
  fetchAgents: vi.fn(async () => agents),
  createRetirementPlan: vi.fn(async () => plan)
}));

describe("RetirementJourneyPage", () => {
  it("lists the agent crew and renders the generated plan", async () => {
    render(<RetirementJourneyPage />);

    await waitFor(() => {
      expect(screen.getByText(/Household Profile Agent/)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: /Generate retirement journey/i }));

    await waitFor(() => {
      expect(screen.getByText(/Murphy household is 1 year from retirement/)).toBeInTheDocument();
    });

    expect(screen.getByText("CONSERVATIVE")).toBeInTheDocument();
    expect(screen.getByText(/Liquidity/)).toBeInTheDocument();
    expect(screen.getByText(/Confirm the retirement date and baseline/)).toBeInTheDocument();
    expect(screen.getByText(/Projections are illustrative only/)).toBeInTheDocument();
  });
});