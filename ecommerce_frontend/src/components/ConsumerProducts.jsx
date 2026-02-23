
import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import { Heart, HeartFill } from "react-bootstrap-icons";

const ConsumerProducts = () => {
  // --- 1. ALL HOOKS AT THE TOP (No Changes Here) ---
  const [products, setProducts] = useState([]);
  const [trending, setTrending] = useState([]);
  const [recentlyViewed, setRecentlyViewed] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [wishlist, setWishlist] = useState([]);
  const [searchQuery, setSearchQuery] = useState(localStorage.getItem("searchQuery") || "");
  const [searchResults, setSearchResults] = useState([]);
  const [brandFilter, setBrandFilter] = useState("");
const [sortOrder, setSortOrder] = useState(""); // "" | "lowHigh" | "highLow"
  const navigate = useNavigate();
  const username = localStorage.getItem("username");

  // --- 2. DATA FETCHING ---
  useEffect(() => {
    const fetchAll = async () => {
      try {
        const response = await axios.get("http://localhost:8090/consumers/products");
        setProducts(response.data);

        // Fetch Wishlist from LocalStorage
        const savedWishlist = JSON.parse(localStorage.getItem(`wishlist_${username}`)) || [];
        setWishlist(savedWishlist);

        // Trending
        const trendingRes = await axios.get("http://localhost:8095/recommendations/trending");
        if (trendingRes.data?.productIds) {
          const promises = trendingRes.data.productIds
            .filter(id => id && id !== "undefined" && id !== "null")
            .map(id => axios.get(`http://localhost:8090/consumers/products/${id}`).then(r => r.data).catch(() => null));
          const res = await Promise.all(promises);
          setTrending(res.filter(p => p !== null));
        }

        // Recent
        if (username) {
          const recentRes = await axios.get(`http://localhost:8095/recommendations/recent/${username}`);
          if (recentRes.data?.productIds) {
            const promises = recentRes.data.productIds
              .filter(id => id && id !== "undefined" && id !== "null")
              .map(id => axios.get(`http://localhost:8090/consumers/products/${id}`).then(r => r.data).catch(() => null));
            const res = await Promise.all(promises);
            setRecentlyViewed(res.filter(p => p !== null));
          }
        }
        setLoading(false);
      } catch (err) {
        setError("Failed to load products.");
        setLoading(false);
      }
    };
    fetchAll();
  }, [username]);

  // Sync Search Query from LocalStorage
  useEffect(() => {
    const handleSearchUpdate = () => setSearchQuery(localStorage.getItem("searchQuery") || "");
    window.addEventListener("searchUpdated", handleSearchUpdate);
    return () => window.removeEventListener("searchUpdated", handleSearchUpdate);
  }, []);

  // --- 2. REDIS SEARCH LOGIC (Updated to match your exact CURL output) ---
useEffect(() => {
  if (searchQuery.trim() === "") {
    setSearchResults([]);
    return;
  }

  const fetchFromRedis = async () => {
    const cleanQuery = searchQuery && searchQuery.trim() !== "" ? searchQuery : "*";
    try {
      const response = await axios.get(`http://localhost:8095/api/search`, {
            params: { query: cleanQuery } 
        });

        // Response structure check karein
        if (response.data && response.data.documents) {
            const formattedResults = response.data.documents.map(doc => {
                const productObj = {};
                // Extracting ID correctly
                productObj.productId = doc.id.split(":").pop();
                
                // Flatten properties array
                doc.properties.forEach(prop => {
                    const key = Object.keys(prop)[0];
                    productObj[key] = prop[key];
                });
                return productObj;
            });
            setSearchResults(formattedResults);
        } else {
            setSearchResults([]);
        }
    } catch (err) {
      console.error("Redis Search Error", err);
      setSearchResults([]);
    }
  };

  const timeoutId = setTimeout(fetchFromRedis, 300);
  return () => clearTimeout(timeoutId);
}, [searchQuery]);

  // --- 3. WISHLIST LOGIC ---
  const toggleWishlist = (e, product) => {
    e.stopPropagation(); // Prevents navigating to details page
    if (!username) return alert("Please login!");

    let updated = [...wishlist];
    const index = updated.findIndex(p => p.productId === product.productId);
    
    if (index > -1) {
      updated.splice(index, 1);
    } else {
      updated.push(product);
    }
    
    setWishlist(updated);
    localStorage.setItem(`wishlist_${username}`, JSON.stringify(updated));
  };

  const isLiked = (id) => wishlist.some(p => p.productId === id);

  // --- 4. SEARCH FILTERING ---
  const filteredProducts = products.filter(p => 
    p.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    p.brand?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    p.category?.toLowerCase().includes(searchQuery.toLowerCase())||
    p.description?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // --- 5. RENDER CARD ---
  const renderProductCard = (product, keyPrefix) => (
    <div className="col-6 col-md-4 col-lg-2 mb-4" key={`${keyPrefix}-${product.productId}`}>
      <div className="card shadow-sm h-100 position-relative" onClick={() => navigate(`/viewProductDetails/${product.productId}`)}>
        
        {/* Wishlist Heart */}
        <button 
          className="btn position-absolute end-0 top-0 m-2 border-0 bg-transparent"
          style={{ zIndex: 10 }}
          onClick={(e) => toggleWishlist(e, product)}
        >
          {isLiked(product.productId) ? <HeartFill color="red" size={20}/> : <Heart color="gray" size={20}/>}
        </button>

        <img 
          src={product.imageUrl ? `http://localhost:8092${product.imageUrl}` : "No Image"} 
          className="card-img-top p-2" 
          style={{height: "160px", objectFit: "contain"}} 
        />
        <div className="card-body p-2 text-center">
          <p className="fw-bold mb-0 text-truncate small">{product.name}</p>
          <p className="text-muted small">₹{product.price}</p>
        </div>
      </div>
    </div>
  );
const getProcessedResults = () => {
  let list = [...searchResults];
  if (brandFilter) {
    list = list.filter(p => p.brand?.toLowerCase().includes(brandFilter.toLowerCase()));
  }
  if (sortOrder === "lowHigh") list.sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
  if (sortOrder === "highLow") list.sort((a, b) => parseFloat(b.price) - parseFloat(a.price));
  return list;
};
  if (loading) return <div className="text-center p-5">Loading Products...</div>;

  return (
    <div className="container py-4">
      {/* If Searching, show only Search Results */}
      {searchQuery.trim() !== "" ? (
  <div className="row">
    {/* Fixed Interactive Sidebar */}
    <div className="col-md-2 border-end">
      <div className="sticky-top" style={{ top: "20px" }}>
        <h5 className="fw-bold mb-3">Filter & Sort</h5>
        <div className="mb-3">
          <label className="small fw-bold">Brand</label>
          <input 
            type="text" 
            className="form-control form-control-sm" 
            placeholder="Type brand..." 
            value={brandFilter} 
            onChange={(e) => setBrandFilter(e.target.value)} 
          />
        </div>
        <div className="mb-3">
          <label className="small fw-bold">Price </label>
          <select className="form-select form-select-sm" onChange={(e) => setSortOrder(e.target.value)}>
            <option value="">Default</option>
            <option value="lowHigh">Price: Low to High</option>
            <option value="highLow">Price: High to Low</option>
          </select>
        </div>
      </div>
    </div>

    {/* Search Results Display */}
    <div className="col-md-9">
      <h3 className="fw-bold mb-4">Search Results for "{searchQuery}"</h3>
      <div className="row">
        {getProcessedResults().length > 0 ? (
          getProcessedResults().map(p => renderProductCard(p, "search"))
        ) : (
          <p className="text-center mt-5">No results found for {searchQuery}.</p>
        )}
      </div>
    </div>
  </div>
) : (
  <>
          {trending.length > 0 && (
            <section className="mb-5">
              <h4 className="fw-bold mb-3">Trending</h4>
              <div className="row">{trending.slice(0, 6).map(p => renderProductCard(p, "trend"))}</div>
            </section>
          )}

          {recentlyViewed.length > 0 && (
            <section className="mb-5">
              <h4 className="fw-bold mb-3">Recently Viewed</h4>
              <div className="row">{recentlyViewed.slice(0, 6).map(p => renderProductCard(p, "recent"))}</div>
            </section>
          )}

          <section>
            <h4 className="fw-bold mb-3">All Products</h4>
            <div className="row">{products.map(p => renderProductCard(p, "all"))}</div>
          </section>
        </>
    )}
    </div>
  );
};

export default ConsumerProducts;