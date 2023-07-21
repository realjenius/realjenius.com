export function onRequestGet(context) {
  return `[LOGGING FROM /hello]: Request came from ${context.request.url}`;
}  