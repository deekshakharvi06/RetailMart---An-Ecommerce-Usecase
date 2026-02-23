import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";

const Login = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });

  const [error, setError] = useState("");

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const { username, password } = formData;
    const consumerLoginUrl = "http://localhost:8090/consumers/login";
    const sellerLoginUrl = "http://localhost:8091/sellers/login";

    try {
      // Try Consumer login first
      const consumerRes = await axios.post(consumerLoginUrl, { username, password });
      const consumerMsg = consumerRes.data?.toString().toLowerCase();

      if (consumerMsg.includes("login successful")) {
        localStorage.setItem("username", username);
        localStorage.setItem("role", "CONSUMER");
        alert("Login successful as Consumer!");
        navigate("/consumerProducts");
        return;
      } else if (
        consumerMsg.includes("invalid credentials") ||
        consumerMsg.includes("consumer doesnot exist")
      ) {
        //Try Seller login if Consumer failed
        const sellerRes = await axios.post(sellerLoginUrl, { username, password });
        const sellerMsg = sellerRes.data?.toString().toLowerCase();

        if (sellerMsg.includes("login successful")) {
          localStorage.setItem("username", username);
          localStorage.setItem("role", "SELLER");
          alert("Login successful as Seller!");
          navigate("/sellerProducts");
          return;
        } else {
          throw new Error("Invalid username or password.");
        }
      } else {
        throw new Error("Invalid username or password.");
      }
    } catch (err) {
      setError("Invalid username or password.");
    }
  };

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100">
      <div className="card shadow-lg p-4" style={{ width: "420px", borderRadius: "12px" }}>
        <h3 className="text-center mb-3 fw-bold">Login</h3>
        <p className="text-muted text-center mb-4">Sign in to your account</p>

        <form onSubmit={handleSubmit}>
          {/* Username */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Username</label>
            <input
              type="text"
              name="username"
              className="form-control"
              placeholder="Enter your username"
              value={formData.username}
              onChange={handleChange}
              required
            />
          </div>

          {/* Password */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Password</label>
            <input
              type="password"
              name="password"
              className="form-control"
              placeholder="Enter your password"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          <button type="submit" className="btn btn-primary w-100 fw-semibold">
            Login
          </button>
        </form>

        {error && <div className="alert alert-danger mt-3">{error}</div>}

        <div className="text-center mt-3">
          <p className="text-muted">
            Don’t have an account?{" "}
            <span
              style={{ color: "#0d6efd", cursor: "pointer" }}
              onClick={() => navigate("/register")}
            >
              Register
            </span>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
