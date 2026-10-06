const DEFAULT_BASE_URL = "http://localhost:8080/demo/api/v1/retirement-journey";

const API_BASE_URL = (import.meta.env.VITE_RETIREMENT_API_BASE_URL || DEFAULT_BASE_URL).replace(/\/$/, "");

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

export async function fetchAgents() {
  const response = await fetch(`${API_BASE_URL}/agents`);
  return parseJson(response);
}

export async function createRetirementPlan(payload) {
  const response = await fetch(`${API_BASE_URL}/plan`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
  return parseJson(response);
}