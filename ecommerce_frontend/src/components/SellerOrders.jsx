import React, { useEffect, useState } from "react";
import axios from "axios";

const SellerOrders = () => {
  const username = localStorage.getItem("username");
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(null);

  //Define logical next statuses
  const getNextStatuses = (currentStatus) => {
    switch (currentStatus?.toUpperCase()) {
      case "PLACED":
        return ["PROCESSING", "SHIPPED", "OUTFORDELIVERY", "DELIVERED", "CANCEL"];
      case "PROCESSING":
        return ["SHIPPED", "OUTFORDELIVERY", "DELIVERED", "CANCEL"];
      case "SHIPPED":
        return ["OUTFORDELIVERY", "DELIVERED", "CANCEL"];
      case "OUTFORDELIVERY":
        return ["DELIVERED", "CANCEL"];
      default:
        return []; // For DELIVERED or CANCELLED
    }
  };

  //Fetch all seller orders
  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8091/sellers/${username}/orders`
        );
        const orderData = res.data;

        const withProductData = await Promise.all(
          orderData.map(async (order) => {
            try {
              const productRes = await axios.get(
                `http://localhost:8091/sellers/${username}/products/${order.productId}`
              );
              return { ...order, product: productRes.data };
            } catch {
              return { ...order, product: null };
            }
          })
        );

        setOrders(withProductData);
      } catch (err) {
        console.error("Error fetching seller orders:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, [username]);

  const formatDate = (date) => {
    if (!date) return "";
    return new Date(date).toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "long",
      year: "numeric",
    });
  };

  // Update Status Function
  const handleUpdateStatus = async (orderId, newStatus) => {
    if (!newStatus) return;
    if (!window.confirm(`Change status to "${newStatus}"?`)) return;

    setUpdating(orderId);
    try {
      // Update order
      const updateRes = await axios.put(
        `http://localhost:8091/sellers/${username}/orders/${orderId}/updateStatus`,
        { status: newStatus },
        { validateStatus: () => true }
      );

      if (updateRes.status >= 200 && updateRes.status < 300) {
        console.log("Order status updated successfully:", updateRes.status);

        //Log entry
        try {
          const oldStatus = orders.find((o) => o.orderId === orderId)?.status;
          const logMessage =
            newStatus === "CANCEL"
              ? "Order cancelled by Seller"
              : `Order marked as ${newStatus.toLowerCase()}`;

          const logRes = await axios.post(
            `http://localhost:8094/orderStatusLog`,
            {
              orderId,
              oldStatus,
              newStatus:
                newStatus === "CANCEL" ? "CANCELLED_BY_SELLER" : newStatus,
              message: logMessage,
              timestamp: new Date().toISOString(),
            },
            { validateStatus: () => true }
          );

          if (logRes.status >= 200 && logRes.status < 300) {
            console.log("📘 Log entry created successfully:", logRes.status);
          } else {
            console.warn("Log service returned non-OK:", logRes.status);
          }
        } catch (logErr) {
          console.warn("Could not create log (network issue):", logErr.message);
        }

        // 3️⃣ Update UI instantly
        setOrders((prev) =>
          prev.map((o) =>
            o.orderId === orderId
              ? {
                  ...o,
                  status:
                    newStatus === "CANCEL"
                      ? "CANCELLED_BY_SELLER"
                      : newStatus,
                }
              : o
          )
        );

        alert("Order status updated successfully!");
      } else {
        console.error("Unexpected response:", updateRes.status);
        alert("Could not update order status — unexpected server response.");
      }
    } catch (err) {
      console.error("Error updating order:", err);
      alert("Could not update order status (network or server error).");
    } finally {
      setUpdating(null);
    }
  };

  //UI Rendering
  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  return (
    <div className="container py-5">
      <h3 className="fw-bold text-primary mb-4 text-center">Your Orders</h3>
      <div className="row g-4">
        {orders.length === 0 ? (
          <div className="text-center text-muted">No orders found.</div>
        ) : (
          orders.map((order) => {
            const currentStatus = order.status?.toUpperCase();
            const availableStatuses = getNextStatuses(currentStatus);

            return (
              <div className="col-md-3" key={order.orderId}>
                <div
                  className="card shadow-sm border p-3 h-100"
                  style={{ borderColor: "#dee2e6", borderRadius: "10px" }}
                >
                  {/* Image */}
                  <div className="text-center mb-3">
                    {order.product?.imageUrl ? (
                      <img
                        src={`http://localhost:8092${order.product.imageUrl}`}
                        alt={order.product.name}
                        style={{
                          height: "120px",
                          width: "100%",
                          objectFit: "contain",
                          borderRadius: "5px",
                        }}
                      />
                    ) : (
                      <div className="text-muted small py-4">
                        No Image Available
                      </div>
                    )}
                  </div>

                  {/* Info */}
                  <h5 className="fw-bold text-primary">
                    Order #{order.orderId}
                  </h5>
                  <p className="mb-1">
                    <strong>Consumer:</strong> {order.username}
                  </p>
                  <p className="mb-1">
                    <strong>Product:</strong>{" "}
                    {order.product?.name || "Product not found"}
                  </p>
                  <p className="mb-1">
                    <strong>Quantity:</strong> {order.quantity}
                  </p>
                  <p className="mb-1">
                    <strong>Total:</strong> ₹{order.totalAmount}
                  </p>

                  {/* Status */}
                  <p className="mb-1">
                    <strong>Status:</strong>{" "}
                    {currentStatus?.includes("CANCELLED_BY_SELLER") ? (
                      <span
                        className="badge bg-warning text-dark px-2"
                        style={{ fontSize: "0.85rem" }}
                      >
                        CANCELLED BY SELLER
                      </span>
                    ) : currentStatus?.includes("CANCELLED") ? (
                      <span
                        className="badge bg-danger text-light px-2"
                        style={{ fontSize: "0.85rem" }}
                      >
                        CANCELLED BY CONSUMER
                      </span>
                    ) : currentStatus === "DELIVERED" ? (
                      <span
                        className="badge bg-success text-light px-2"
                        style={{ fontSize: "0.85rem" }}
                      >
                        DELIVERED
                      </span>
                    ) : (
                      <span
                        className="badge bg-secondary text-light px-2"
                        style={{ fontSize: "0.85rem" }}
                      >
                        {order.status || "PLACED"}
                      </span>
                    )}
                  </p>

                  {/* Note for Cancelled */}
                  {currentStatus?.includes("CANCELLED_BY_SELLER") && (
                    <div className="text-warning small mt-2">
                      ❌ Cancelled by Seller (No further updates)
                    </div>
                  )}
                  {currentStatus?.includes("CANCELLED") &&
                    !currentStatus?.includes("SELLER") && (
                      <div className="text-danger small mt-2">
                        ❌ Cancelled by Consumer (No further updates)
                      </div>
                    )}

                  {/* Dropdown */}
                  {!currentStatus?.includes("CANCELLED") &&
                    currentStatus !== "DELIVERED" && (
                      <div className="mt-3">
                        <select
                          className="form-select"
                          onChange={(e) =>
                            handleUpdateStatus(order.orderId, e.target.value)
                          }
                          defaultValue=""
                          disabled={updating === order.orderId}
                        >
                          <option value="">-- Update Status --</option>
                          {availableStatuses.map((status) => (
                            <option key={status} value={status}>
                              {status}
                            </option>
                          ))}
                        </select>
                      </div>
                    )}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default SellerOrders;
