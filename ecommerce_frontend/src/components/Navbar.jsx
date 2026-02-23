import React, { useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import { Search, X } from "react-bootstrap-icons";

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const username = localStorage.getItem("username");
  const role = localStorage.getItem("role");

  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const [searchQuery, setSearchQuery] = useState(localStorage.getItem("searchQuery") || "");

  const handleSearchChange = (e) => {
    const q = e.target.value;
    setSearchQuery(q);
    localStorage.setItem("searchQuery", q);
    window.dispatchEvent(new Event("searchUpdated"));

    if (location.pathname !== "/consumerProducts") {
      navigate("/consumerProducts");
    }
  };

  const clearSearch = () => {
    setSearchQuery("");
    localStorage.removeItem("searchQuery");
    window.dispatchEvent(new Event("searchUpdated"));
  };

  const handleLogout = () => {
    const confirmLogout = window.confirm("Are you sure you want to logout?");
    if (confirmLogout) {
      localStorage.removeItem("username");
      localStorage.removeItem("role");
      navigate("/login");
    }
  };

  const handleProfileClick = () => navigate("/userProfile");

  const [suggestions, setSuggestions] = useState([]);

useEffect(() => {
  const fetchSuggestions = async () => {
    if (!searchQuery.trim()) return setSuggestions([]);
    try {
      const res = await axios.get(`http://localhost:8095/search/suggest?prefix=${searchQuery}`);
      setSuggestions(res.data);
    } catch {
      setSuggestions([]);
    }
  };
  const debounce = setTimeout(fetchSuggestions, 300);
  return () => clearTimeout(debounce);
}, [searchQuery]);


  return (
    <nav className="navbar navbar-expand-md navbar-dark bg-dark px-3 px-md-4">
      <div className="container-fluid">
        {/* LEFT - Brand */}
        <Link className="navbar-brand fw-bold me-3" to="/">
          🛍️RetailMart
        </Link>

        {/* MOBILE SEARCH beside logo (visible only on small screens) */}

        {/* Hamburger (mobile menu) */}
        <button
          className="navbar-toggler"
          type="button"
          onClick={() => setIsMenuOpen(!isMenuOpen)}
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        {/* MAIN MENU */}
        <div className={`collapse navbar-collapse ${isMenuOpen ? "show" : ""}`}>
          <div className="d-flex flex-column flex-md-row align-items-center w-100 justify-content-between">
            
            {/* CENTER - Nav Links */}
            <ul className="navbar-nav me-md-4 mb-2 mb-md-0">
              {username && (
                <>
                  <li className="nav-item">
                    <Link
                      className="nav-link"
                      to="/"
                      onClick={() => setIsMenuOpen(false)}
                    >
                      Home
                    </Link>
                  </li>

                  {role === "SELLER" ? (
                    <li className="nav-item">
                      <Link
                        className="nav-link"
                        to="/sellerProducts"
                        onClick={() => setIsMenuOpen(false)}
                      >
                        Products
                      </Link>
                    </li>
                  ) : (
                    <li className="nav-item">
                      <Link
                        className="nav-link"
                        to="/consumerProducts"
                        onClick={() => setIsMenuOpen(false)}
                      >
                        Products
                      </Link>
                    </li>
                  )}

                  {role === "SELLER" ? (
                    <li className="nav-item">
                      <Link
                        className="nav-link"
                        to="/sellerOrders"
                        onClick={() => setIsMenuOpen(false)}
                      >
                        Orders
                      </Link>
                    </li>
                  ) : (
                    <>
                      <li className="nav-item">
                        <Link
                          className="nav-link"
                          to="/consumerOrders"
                          onClick={() => setIsMenuOpen(false)}
                        >
                          Orders
                        </Link>
                      </li>
                      <li className="nav-item">
                        <Link
                          className="nav-link"
                          to="/wishlist"
                          onClick={() => setIsMenuOpen(false)}
                        >
                          Wishlist
                        </Link>
                      </li>
                    </>
                  )}
                </>
              )}
            </ul>

            {/* DESKTOP SEARCH BAR (hidden on small screens) */}
            {role === "CONSUMER" && (
              <form
                onSubmit={(e) => {
                e.preventDefault();
                localStorage.setItem("searchQuery", searchQuery);
                window.dispatchEvent(new Event("searchUpdated"));
                if (location.pathname !== "/consumerProducts") {
                  navigate("/consumerProducts");
                }
              }}
              className="position-relative mx-auto my-2 my-md-0 d-none d-md-block"
              style={{ width: "420px" }}
              >
                <input
                  type="text"
                  placeholder="Search for Products, Brands and More"
                  value={searchQuery}
                  onChange={handleSearchChange}
                  className="form-control rounded-pill ps-3 pe-5"
                  style={{
                    backgroundColor: "#f0f5ff",
                    border: "none",
                    color: "black",
                    paddingRight: "4rem",
                  }}
                />
                {searchQuery && (
                  <button
                    type="button"
                    className="btn btn-link text-muted position-absolute top-50 translate-middle-y"
                    onClick={clearSearch}
                    style={{
                      right: "2.5rem",
                      textDecoration: "none",
                      zIndex: 10,
                    }}
                  >
                    <X size={16} />
                  </button>
                )}
                <button
                  type="submit"
                  className="btn position-absolute top-50 end-0 translate-middle-y me-2 text-primary p-0"
                  style={{ background: "transparent", border: "none" }}
                >
                  <Search size={18} />
                </button>
              </form>
            )}

            {/* RIGHT - Profile + Logout */}
            <div className="d-flex align-items-center mt-3 mt-md-0">
              {username ? (
                <>
                  <div
                    className="text-white me-3 d-flex align-items-center"
                    style={{ cursor: "pointer" }}
                    onClick={handleProfileClick}
                  >
                    👤 <span className="ms-1">{username}</span>
                  </div>
                  <button
                    className="btn btn-outline-light"
                    onClick={() => {
                      handleLogout();
                      setIsMenuOpen(false);
                    }}
                  >
                    Logout
                  </button>
                </>
              ) : (
                <>
                  <Link
                    className="btn btn-outline-light me-2"
                    to="/login"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Login
                  </Link>
                  <Link
                    className="btn btn-primary"
                    to="/register"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Register
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
