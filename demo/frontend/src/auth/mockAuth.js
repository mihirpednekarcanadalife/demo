/**
 * Mock authentication for the customer portal demo.
 *
 * The username is the case `owner` value - signing in simply scopes the case list
 * to cases owned by that user. Never use this for anything real.
 */
const MOCK_PASSWORD = import.meta.env.VITE_PORTAL_MOCK_PASSWORD || "pension123";

export const MOCK_CREDENTIALS_HINT = `Any username, password: ${MOCK_PASSWORD}`;

export function signIn(username, password) {
  if (!username || !username.trim()) {
    throw new Error("Username is required");
  }
  if (password !== MOCK_PASSWORD) {
    throw new Error("Invalid username or password");
  }
  return { username: username.trim() };
}