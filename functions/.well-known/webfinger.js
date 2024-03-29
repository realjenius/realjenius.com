export function onRequestGet(context) {
  return new Response(`
    {
      "subject": "acct:rj@realjenius.com",
      "links": [{
        "rel": "http://openid.net/specs/connect/1.0/issuer",
        "href": "https://auth.realjenius.com"
      }]
    }`, {
      status: 200,
      headers: {
        "Content-Type": "application/json"
      }
    });
}