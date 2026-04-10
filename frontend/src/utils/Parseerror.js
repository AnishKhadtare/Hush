/**
 * Tries to extract a readable message from an API error.
 * Backend may return JSON like {"message":"..."} or plain text.
 */
export function parseError(err, fallback = "Something went wrong. Please try again.") {
  try {
    const parsed = JSON.parse(err.message);
    return parsed.message ?? parsed.error ?? fallback;
  } catch {
    return err.message || fallback;
  }
}
