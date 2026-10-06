const DEFAULT_BASE_URL = "http://localhost:8080/demo/api/v1/policies";

const API_BASE_URL = (import.meta.env.VITE_POLICY_API_BASE_URL || DEFAULT_BASE_URL).replace(/\/$/, "");

async function parseJson(response) {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
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

/** GET /api/v1/policies/{policyId} */
export async function fetchPolicy(policyId) {
  const response = await fetch(`${API_BASE_URL}/${encodeURIComponent(policyId)}`);
  return parseJson(response);
}