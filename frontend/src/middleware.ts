import { NextRequest, NextResponse } from 'next/server';

// List of routes that do not require authentication
const publicRoutes = ['/login'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Allow public routes
  if (publicRoutes.includes(pathname)) {
    return NextResponse.next();
  }

  // Check for a mock signed-in cookie
  const signedinCookie = request.cookies.get('signedin');
  const isSignedIn = signedinCookie && signedinCookie.value === 'true';

  if (!isSignedIn) {
    const loginUrl = request.nextUrl.clone();
    loginUrl.pathname = '/login';
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!_next|api|public).*)'],
};
