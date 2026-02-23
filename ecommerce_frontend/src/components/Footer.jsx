import React from "react";
import "bootstrap/dist/css/bootstrap.min.css";

const Footer = () => {
  return (
    <footer className="bg-dark text-light py-3 mt-auto">
      <div className="container text-center">
        <p className="mb-1">
          © {new Date().getFullYear()} <strong>RetailMart</strong>. All Rights Reserved.
        </p>
      </div>
    </footer>
  );
};

export default Footer;
