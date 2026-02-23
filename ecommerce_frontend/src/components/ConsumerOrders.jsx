import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { ChevronRight, BoxSeam, ArrowLeft } from "react-bootstrap-icons";

const ConsumerOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const username = localStorage.getItem("username");

  useEffect(() => {
    const fetchOrdersAndProducts = async () => {
      try {
        // Fetch all orders of the consumer
        const ordersRes = await axios.get(
          `http://localhost:8090/consumers/${username}/orders`
        );
        const orderData = ordersRes.data;

        // For each order, fetch product details from consumer product API
        const enrichedOrders = await Promise.all(
          orderData.map(async (order) => {
            try {
              const productRes = await axios.get(
                `http://localhost:8090/consumers/products/${order.productId}`
              );
              const product = productRes.data;
              return {
                ...order,
                productName: product.name,
                productImageUrl: product.imageUrl,
              };
            } catch (err) {
              console.error("Product fetch failed for:", order.productId);
              return {
                ...order,
                productName: "Unknown Product",
                productImageUrl: null,
              };
            }
          })
        );

        setOrders(enrichedOrders);
      } catch (err) {
        console.error("Error fetching orders:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchOrdersAndProducts();
  }, [username]);

  //badge color
  const getStatusStyle = (status) => {
    switch (status?.toUpperCase()) {
      case "DELIVERED":
        return "bg-success";
      case "CANCELLED":
        return "bg-danger";
      case "SHIPPED":
        return "bg-primary";
      // case "CANCEL":
      //   return "bg-dannger";
      default:
        return "bg-warning text-dark";
    }
  };

  if (loading)
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <div className="spinner-border text-warning" role="status"></div>
      </div>
    );

  return (
    <>
    <div className="border-1 d-flex align-items-left">
    {/*Back Button */}
      <button
        className="btn btn-link text-decoration-none text-dark mb-3 p-0 border-1"
        onClick={() => navigate("/ConsumerProducts")}
      >
        <ArrowLeft /> Back to Products
      </button>
    </div>
    <div className="container py-5 card shadow-sm border-1 p-4 h-100" style={{ maxWidth: "900px" , borderColor: "#dee2e6", borderRadius: "10px"}}>
      <div className="d-flex align-items-center mb-4">
        <BoxSeam size={30} className="me-2 text-warning" />
        <h2 className="fw-bold m-0">My Orders</h2>
      </div>

      {orders.length === 0 ? (
        <div className="text-center py-5 border rounded bg-light">
          <p className="text-muted">You haven't placed any orders yet.</p>
          <button
            className="btn btn-warning"
            onClick={() => navigate("/consumerProducts")}
          >
            Start Shopping
          </button>
        </div>
      ) : (
        <div className="d-flex flex-column gap-3">
          {orders.map((order) => {
            const imageUrl = order.productImageUrl
              ? `http://localhost:8092${order.productImageUrl}`
              : null;

            return (
              <div
                key={order.orderId}
                className="card shadow-sm border-0 overflow-hidden"
              >
                <div className="row g-0 align-items-center">
                  {/* Product Image */}
                  <div
                    className="col-3 col-md-2 bg-light d-flex align-items-center justify-content-center p-2"
                    style={{ minHeight: "120px" }}
                  >
                    {imageUrl ? (
                      <img
                        src={imageUrl}
                        alt={order.productName}
                        className="img-fluid rounded"
                        style={{ maxHeight: "100px", objectFit: "contain" }}
                      />
                    ) : (
                      <div className="text-muted small">No Image</div>
                    )}
                  </div>

                  {/*Product Info */}
                  <div className="col-6 col-md-7 p-3">
                    <h5 className="fw-bold mb-2">{order.productName}</h5>
                    <span
                      className={`badge ${getStatusStyle(order.status)} px-2 py-1 text-capitalize`}
                    >
                      {order.status || "Pending"}
                    </span>
                  </div>

                  {/* View Details Button */}
                  <div className="col-3 col-md-3 text-end p-3">
                    <button
                      className="btn btn-outline-dark btn-sm d-flex align-items-center gap-1 ms-auto"
                      onClick={() =>
                        navigate(`/viewOrderDetails/${order.orderId}`)
                      }
                    >
                      View Details <ChevronRight size={14} />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
    </>
  );
};

export default ConsumerOrders;
