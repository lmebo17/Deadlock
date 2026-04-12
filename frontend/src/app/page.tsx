import { Navbar } from "@/components/Navbar";

export default function Home() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <div className="flex min-h-screen items-center justify-center pt-16">
        <div className="text-center">
          <h1 className="text-5xl font-bold tracking-tight sm:text-7xl">
            Dead<span className="text-primary">lock</span>
          </h1>
          <p className="mt-4 text-lg text-muted-foreground">
            1v1 Competitive Programming Platform
          </p>
        </div>
      </div>
    </div>
  );
}
