import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Header from "@/src/components/common/Header";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "SubMeNote",
  description: "SubMeNote Platform",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-[#161620]`}
      >
        <Header />
        <main className="min-h-screen relative">
          {/* Animated gradient orbs - Dark theme - 톤다운 */}
          <div className="fixed top-0 left-0 w-full h-full overflow-hidden pointer-events-none -z-10">
            <div className="absolute top-20 left-10 w-96 h-96 bg-purple-500/12 rounded-full filter blur-3xl animate-float"></div>
            <div className="absolute top-40 right-10 w-96 h-96 bg-purple-400/10 rounded-full filter blur-3xl animate-float" style={{animationDelay: '2s'}}></div>
            <div className="absolute -bottom-20 left-1/2 w-96 h-96 bg-blue-500/12 rounded-full filter blur-3xl animate-float" style={{animationDelay: '4s'}}></div>
            <div className="absolute top-1/2 left-1/4 w-80 h-80 bg-cyan-400/10 rounded-full filter blur-3xl animate-float" style={{animationDelay: '1s'}}></div>
            <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-purple-400/10 rounded-full filter blur-3xl animate-float" style={{animationDelay: '3s'}}></div>
          </div>
          {/* Grid pattern overlay */}
          <div className="fixed top-0 left-0 w-full h-full pointer-events-none -z-10 opacity-8"
               style={{
                 backgroundImage: 'linear-gradient(rgba(157, 122, 255, 0.08) 1px, transparent 1px), linear-gradient(90deg, rgba(157, 122, 255, 0.08) 1px, transparent 1px)',
                 backgroundSize: '50px 50px'
               }}>
          </div>
          {children}
        </main>
      </body>
    </html>
  );
}
