import { useEffect, useMemo, useState } from "react";
import { compareProducts, fetchProducts } from "../api/pensionApi";
import ComparisonResults from "../components/ComparisonResults";
import CriteriaForm from "../components/CriteriaForm";
import ProductSelector from "../components/ProductSelector";

const defaultForm = {
  currentAge: 35,
  retirementAge: 65,
  initialFundValueEuros: 50000,
  regularMonthlyContributionEuros: 400,
  singleAnnualContributionEuros: 2000,
  compoundAnnualGrowthRateBeforePrsaFeesPercent: 5.5,
  feeWeight: 0.4,
  creditRatingWeight: 0.3,
  flexibilityWeight: 0.2,
  digitalServicesWeight: 0.1
};

function toNumber(value) {
  const parsed = Number(value);
  return Number.isNaN(parsed) ? 0 : parsed;
}

export default function ComparisonPage() {
  const [products, setProducts] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [comparisonResult, setComparisonResult] = useState(null);
  const [form, setForm] = useState(defaultForm);
  const [loading, setLoading] = useState(true);
  const [comparing, setComparing] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadProducts() {
      try {
        setLoading(true);
        setError("");
        const data = await fetchProducts();
        setProducts(data);
      } catch (e) {
        setError(e.message || "Failed to load products");
      } finally {
        setLoading(false);
      }
    }

    loadProducts();
  }, []);

  const canCompare = useMemo(() => selectedIds.length >= 2 && !comparing, [selectedIds.length, comparing]);

  function toggleProduct(productId) {
    setSelectedIds((previous) =>
      previous.includes(productId)
        ? previous.filter((id) => id !== productId)
        : [...previous, productId]
    );
  }

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((previous) => ({
      ...previous,
      [name]: value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (selectedIds.length < 2) {
      setError("Please select at least 2 products.");
      return;
    }

    const payload = {
      productIds: selectedIds,
      currentAge: toNumber(form.currentAge),
      retirementAge: toNumber(form.retirementAge),
      initialFundValueEuros: toNumber(form.initialFundValueEuros),
      regularMonthlyContributionEuros: toNumber(form.regularMonthlyContributionEuros),
      singleAnnualContributionEuros: toNumber(form.singleAnnualContributionEuros),
      compoundAnnualGrowthRateBeforePrsaFeesPercent: toNumber(form.compoundAnnualGrowthRateBeforePrsaFeesPercent),
      weights: {
        feeWeight: toNumber(form.feeWeight),
        creditRatingWeight: toNumber(form.creditRatingWeight),
        flexibilityWeight: toNumber(form.flexibilityWeight),
        digitalServicesWeight: toNumber(form.digitalServicesWeight)
      }
    };

    try {
      setComparing(true);
      setError("");
      const result = await compareProducts(payload);
      setComparisonResult(result);
    } catch (e) {
      setError(e.message || "Failed to compare products");
    } finally {
      setComparing(false);
    }
  }

  return (
    <section>
      <h1>Pension Product Comparison</h1>
      <p>Select products and compare across fee impact, credit strength, flexibility, and digital services.</p>

      {loading && <p>Loading products...</p>}
      {error && <p className="error">{error}</p>}

      {!loading && (
        <>
          <ProductSelector products={products} selectedIds={selectedIds} onToggle={toggleProduct} />
          <CriteriaForm form={form} onChange={handleChange} onSubmit={handleSubmit} disabled={!canCompare} />
          <ComparisonResults result={comparisonResult} />
        </>
      )}
    </section>
  );
}