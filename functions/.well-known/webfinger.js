export function onRequestGet(context) {

  const { searchParams } = new URL(context.request.url)
  searchParams.get('rel')
}  