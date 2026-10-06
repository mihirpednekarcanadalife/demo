export default function ProductSelector({ products, selectedIds, onToggle }) {
  return (
    <div className="card">
      <h2>Select Products</h2>
      <p>Pick at least 2 pension products from different providers if possible.</p>
      <div className="grid">
        {products.map((product) => {
          const checked = selectedIds.includes(product.id);
          return (
            <label key={product.id} className="product-option">
              <input
                type="checkbox"
                checked={checked}
                onChange={() => onToggle(product.id)}
              />
              <span className="product-option-text">
                {product.name} ({product.provider}) - Fee {product.annualFeePercent}%
              </span>
            </label>
          );
        })}
      </div>
    </div>
  );
}