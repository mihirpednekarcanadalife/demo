import { useState } from "react";
import ComparisonPage from "./pages/ComparisonPage";
import RetirementJourneyPage from "./pages/RetirementJourneyPage";
import CaseWorkflowPage from "./pages/CaseWorkflowPage";
import CustomerPortalPage from "./pages/CustomerPortalPage";
import EmailPage from "./pages/EmailPage";

const TABS = [
  { id: "compare", label: "Compare products" },
  { id: "journey", label: "Retirement journey" },
  { id: "cases", label: "Case workflow" },
  { id: "portal", label: "Customer portal" },
  { id: "email", label: "Email" }
];

const PAGES = {
  compare: ComparisonPage,
  journey: RetirementJourneyPage,
  cases: CaseWorkflowPage,
  portal: CustomerPortalPage,
  email: EmailPage
};

export default function App() {
  const [activeTab, setActiveTab] = useState("compare");
  const ActivePage = PAGES[activeTab] ?? ComparisonPage;

  return (
    <main className="container">
      <nav className="tabs" aria-label="Main navigation">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            className={activeTab === tab.id ? "tab tab-active" : "tab"}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <ActivePage onNavigate={setActiveTab} />

    </main>
  );
}