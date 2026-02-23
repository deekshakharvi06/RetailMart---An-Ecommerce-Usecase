import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";

const Register = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    username: "",
    password: "",
    location: "",
    role: "CONSUMER", // default
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Handle input change
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  // Password validation
  const validatePassword = (password) => {
    const regex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    return regex.test(password);
  };

  // Handle registration
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    const { username, password, location, role } = formData;

    if (!validatePassword(password)) {
      setError(
        "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
      );
      return;
    }

    try {
      const consumerCheck = await axios
        .get(`http://localhost:8090/consumers/${username}`)
        .then(() => true)
        .catch(() => false); // false means not found (ok)

      const sellerCheck = await axios
        .get(`http://localhost:8091/sellers/${username}`)
        .then(() => true)
        .catch(() => false);

      if (consumerCheck || sellerCheck) {
        setError("Username already exists. Please choose a different one.");
        return;
      }

      const url =
        role === "SELLER"
          ? "http://localhost:8091/sellers/register"
          : "http://localhost:8090/consumers/register";

      const response = await axios.post(url, {
        username,
        password,
        location,
        role,
      });

      if (response.status === 201 || response.status === 200) {
        setSuccess("Registration successful!");
        alert("Registration successful!");
        navigate("/login");
      }
    } catch (err) {
      console.error("Registration error:", err);
      setError(
        err.response?.data || "Something went wrong. Please try again later."
      );
    }
  };

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100">
      <div className="card shadow-lg p-4" style={{ width: "420px", borderRadius: "12px" }}>
        <h3 className="text-center mb-3 fw-bold">Register</h3>
        <p className="text-muted text-center mb-4">
          Create an account as Consumer or Seller.
        </p>

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
            <small className="text-muted">
              Must be at least 8 characters and include uppercase, lowercase, number, and special character.
            </small>
          </div>

          {/* Location */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Location</label>
            <input
              type="text"
              name="location"
              className="form-control"
              placeholder="Enter your location"
              value={formData.location}
              onChange={handleChange}
              required
            />
          </div>

          {/* Role */}
          <div className="mb-4">
            <label className="form-label fw-semibold">Role</label>
            <select
              name="role"
              className="form-select"
              value={formData.role}
              onChange={handleChange}
              required
            >
              <option value="CONSUMER">Consumer</option>
              <option value="SELLER">Seller</option>
            </select>
          </div>

          <button type="submit" className="btn btn-primary w-100 fw-semibold">
            Register
          </button>
        </form>

        {error && <div className="alert alert-danger mt-3">{error}</div>}
        {success && <div className="alert alert-success mt-3">{success}</div>}

        <div className="text-center mt-3">
          <p className="text-muted">
            Already have an account?{" "}
            <span
              style={{ color: "#0d6efd", cursor: "pointer" }}
              onClick={() => navigate("/login")}
            >
              Login
            </span>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
