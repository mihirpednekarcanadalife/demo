const DEFAULT_BASE_URL = "http://localhost:8080/demo/api/v1/cases";

const API_BASE_URL = (import.meta.env.VITE_CASE_API_BASE_URL || DEFAULT_BASE_URL).replace(/\/$/, "");

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

/** GET /api/v1/cases - every case in the workflow. */
export async function fetchCases() {
  const response = await fetch(API_BASE_URL);
  return parseJson(response);
}

/** GET /api/v1/cases/{caseId} */
export async function fetchCase(caseId) {
  const response = await fetch(`${API_BASE_URL}/${encodeURIComponent(caseId)}`);
  return parseJson(response);
}

/** GET /api/v1/cases/by-policy/{policyId} */
export async function fetchCaseByPolicy(policyId) {
  const response = await fetch(`${API_BASE_URL}/by-policy/${encodeURIComponent(policyId)}`);
  return parseJson(response);
}