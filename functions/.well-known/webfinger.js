export function onRequestGet(context) {
  return new Response(`[LOGGING FROM /hello]: Request came from ${context.request.url}`); 
}  