export function onRequestGet(context) {
  return new Response(`
    {
      "subject": "acct:tailscale@realjenius.com",
      "links": [{
        "rel": "http://openid.net/specs/connect/1.0/issuer",
        "href": "https://auth.realjenius.com"
      }]
    }`);
}