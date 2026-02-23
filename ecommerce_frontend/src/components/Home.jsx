import React from "react";
import "bootstrap/dist/css/bootstrap.min.css";

const Home = () => {
  const role = localStorage.getItem("role");
  return (
    <div className="home-page">
      <section
        className="text-center text-white d-flex flex-column justify-content-center align-items-center"
        style={{
          background:
            "url('https://th.bing.com/th/id/OIP.ef5nueamOluIsiUfmxgS6gHaE7?w=270&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3') center/cover no-repeat",
          height: "70vh",
          position: "relative",
        }}
      >
        <div
          className="overlay position-absolute w-100 h-100"
          
        ></div>

        <div className="content position-relative text-center">
          <h1 className="fw-bold display-5 mb-3">Welcome to RetailMart 🛍️</h1>
          <p className="lead mb-4">
            A one-stop platform connecting <b>Sellers</b> and <b>Consumers</b> seamlessly.
          </p>
           {/*Role-based button */}
          {role === "SELLER" ? (
            <a href="/sellerProducts" className="btn btn-primary btn-lg">
              View My Products
            </a>
          ) : role === "CONSUMER" ? (
            <a href="/consumerProducts" className="btn btn-primary btn-lg">
              View Products
            </a>
          ) : (
            <a href="/register" className="btn btn-primary btn-lg">
              Get Started
            </a>
          )}
        </div>
      </section>

      {/* ---------- WHY RETAILMART SECTION ---------- */}
      <section className="py-5 bg-light text-center">
        <div className="container">
          <h2 className="fw-bold mb-5">Why RetailMart?</h2>
          <div className="row g-4 justify-content-center">
            {/* Seller Card */}
            <div className="col-md-4">
              <div className="card border-0 shadow-sm h-100">
                <div className="card-body">
                  <h5 className="card-title text-primary fw-bold">👩‍💼 For Sellers</h5>
                  <p className="card-text mt-3 text-muted">
                    Easily add, edit, and manage your products. Track all orders placed by consumers
                    and update order statuses in <b>real time</b>.
                  </p>
                </div>
              </div>
            </div>

            {/* Consumer Card */}
            <div className="col-md-4">
              <div className="card border-0 shadow-sm h-100">
                <div className="card-body">
                  <h5 className="card-title text-success fw-bold">🛒 For Consumers</h5>
                  <p className="card-text mt-3 text-muted">
                    Explore products from multiple sellers, place orders securely, and track your
                    deliveries — all in one place.
                  </p>
                </div>
              </div>
            </div>

            {/* Microservices Card */}
            <div className="col-md-4">
              <div className="card border-0 shadow-sm h-100">
                <div className="card-body">
                  <h5 className="card-title text-warning fw-bold">⚙️ Powered by Microservices</h5>
                  <p className="card-text mt-3 text-muted">
                    RetailMart is built using <b>Spring Boot</b> microservices and <b>React</b> —
                    scalable, reliable, and easy to maintain.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ---------- CTA SECTION ---------- */}
      <section className="text-center py-5" style={{ backgroundColor: "#111", color: "white" }}>
        <h3 className="fw-bold mb-3">Ready to get started?</h3>
        <p className="mb-4 text-white">
          Whether you're a seller or consumer, RetailMart is here to simplify your online experience.
        </p>
        <a href="/register" className="btn btn-primary btn-lg">
          Join Now 🚀
        </a>
      </section>
      </div>
  )
}

export default Home
