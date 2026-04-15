/** @type {import('next').NextConfig} */
const nextConfig = {
  output: "export",
  trailingSlash: true,
  basePath: "/ui",
  assetPrefix: "/ui/",
  images: {
    unoptimized: true
  }
};

export default nextConfig;
