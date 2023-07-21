export function onRequestGet(context) {

  const { searchParams } = new URL(context.request.url)
  return searchParams.get('rel')
}  