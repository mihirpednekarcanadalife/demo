const DEFAULT_BASE_URL = "http://localhost:8080/demo/api/v1/pension-products";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || DEFAULT_BASE_URL).replace(/\/$/, "");

async function parseJson(response) {
  if (!response.ok) {
    let message = "Request failed";
    try {
      const body = await response.json();
      message = body.message || message;
    } catch (error) {
      // Keep default when response is not JSON.
    }
    throw new Error(message);
  }
  return response.json();
}

export async function fetchProducts() {
  const response = await fetch(`${API_BASE_URL}/products`);
  return parseJson(response);
}

export async function compareProducts(payload) {
  const response = await fetch(`${API_BASE_URL}/compare`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
  return parseJson(response);
}