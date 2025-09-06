import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
  const [bubbles, setBubbles] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const handleMouseMove = (e) => {
      setMousePosition({
        x: (e.clientX / window.innerWidth) * 100,
        y: (e.clientY / window.innerHeight) * 100
      });
    };
    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, []);

  useEffect(() => {
    // Generate floating bubbles
    const bubbleInterval = setInterval(() => {
      const newBubble = {
        id: Date.now(),
        x: Math.random() * 100,
        y: 110,
        size: Math.random() * 40 + 20,
        speed: Math.random() * 3 + 1,
        opacity: Math.random() * 0.5 + 0.3
      };
      setBubbles(prev => [...prev.slice(-10), newBubble]);
    }, 1000);

    return () => clearInterval(bubbleInterval);
  }, []);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await authService.login(formData.username, formData.password);
      console.log('Login successful, redirecting to dashboard');
      navigate('/dashboard');
    } catch (error) {
      setError(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container" style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, var(--light-blue) 0%, var(--sky-blue) 30%, var(--ocean-blue) 70%, var(--deep-blue) 100%)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem',
      position: 'relative',
      overflow: 'hidden'
    }}>

      {/* Floating Bubbles */}
      {bubbles.map(bubble => (
        <div
          key={bubble.id}
          className="bubble"
          style={{
            position: 'absolute',
            left: `${bubble.x}%`,
            bottom: `${bubble.y - (Date.now() % 20000) / 100}%`,
            width: `${bubble.size}px`,
            height: `${bubble.size}px`,
            background: `radial-gradient(circle at 30% 30%, rgba(255, 255, 255, ${bubble.opacity}), rgba(135, 206, 235, 0.3))`,
            borderRadius: '50%',
            pointerEvents: 'none',
            animation: `bubbleFloat ${bubble.speed * 5}s linear infinite`,
            border: '1px solid rgba(255, 255, 255, 0.2)'
          }}
        />
      ))}

      {/* Interactive mouse trail */}
      <div
        className="mouse-glow"
        style={{
          position: 'absolute',
          left: `${mousePosition.x}%`,
          top: `${mousePosition.y}%`,
          width: '300px',
          height: '300px',
          background: 'radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%)',
          borderRadius: '50%',
          transform: 'translate(-50%, -50%)',
          pointerEvents: 'none',
          transition: 'all 0.3s ease-out'
        }}
      />

      {/* Animated water waves */}
      <div className="wave-container" style={{
        position: 'absolute',
        bottom: 0,
        left: 0,
        width: '100%',
        height: '200px',
        background: `
          linear-gradient(45deg, transparent 33%, rgba(255, 255, 255, 0.1) 33%, rgba(255, 255, 255, 0.1) 66%, transparent 66%),
          linear-gradient(-45deg, transparent 33%, rgba(255, 255, 255, 0.1) 33%, rgba(255, 255, 255, 0.1) 66%, transparent 66%)
        `,
        backgroundSize: '30px 30px',
        animation: 'waveMove 10s linear infinite',
        opacity: 0.3
      }} />

      {/* Main login form */}
      <div className="login-form-container" style={{
        background: 'rgba(255, 255, 255, 0.15)',
        backdropFilter: 'blur(25px)',
        borderRadius: '30px',
        border: '1px solid rgba(255, 255, 255, 0.3)',
        boxShadow: '0 25px 50px rgba(0, 0, 0, 0.2)',
        width: '100%',
        maxWidth: '450px',
        padding: '3rem',
        position: 'relative',
        zIndex: 2,
        animation: 'slideInUp 0.8s ease-out'
      }}>

        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
          <div className="app-icon" style={{
            width: '90px',
            height: '90px',
            background: 'linear-gradient(45deg, var(--light-blue), var(--sky-blue))',
            borderRadius: '50%',
            margin: '0 auto 1.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 15px 35px rgba(135, 206, 235, 0.4)',
            animation: 'pulseGlow 2s infinite',
            fontSize: '2.5rem',
            position: 'relative',
            overflow: 'hidden'
          }}>
            <span style={{ position: 'relative', zIndex: 2 }}>🌊</span>
            <div style={{
              position: 'absolute',
              top: '0',
              left: '0',
              width: '100%',
              height: '100%',
              background: 'linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent)',
              transform: 'translateX(-100%)',
              animation: 'shimmer 2s infinite'
            }} />
          </div>

          <h1 style={{
            fontSize: '2.8rem',
            fontWeight: 900,
            background: 'linear-gradient(45deg, #FFFFFF 0%, var(--light-blue) 50%, #FFFFFF 100%)',
            backgroundClip: 'text',
            WebkitBackgroundClip: 'text',
            color: 'transparent',
            marginBottom: '0.5rem',
            textShadow: '0 0 30px rgba(255, 255, 255, 0.5)'
          }}>
            🌊 WeatherCast
          </h1>

          <p style={{
            color: 'rgba(255, 255, 255, 0.9)',
            fontSize: '1.2rem',
            fontWeight: 500,
            textShadow: '0 2px 10px rgba(0, 0, 0, 0.3)'
          }}>
            ✨ Welcome back to the future of weather prediction
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '1.5rem'
        }}>

          {error && (
            <div className="error-message" style={{
              background: 'rgba(255, 82, 82, 0.2)',
              border: '1px solid rgba(255, 82, 82, 0.4)',
              borderRadius: '15px',
              padding: '1rem',
              color: '#FFB3B3',
              fontSize: '0.95rem',
              textAlign: 'center',
              animation: 'shake 0.5s ease-in-out',
              backdropFilter: 'blur(10px)'
            }}>
              ⚠️ {error}
            </div>
          )}

          {/* Username Field */}
          <div style={{ position: 'relative' }}>
            <label style={{
              display: 'block',
              fontSize: '1rem',
              fontWeight: 600,
              marginBottom: '0.8rem',
              color: 'rgba(255, 255, 255, 0.9)',
              textShadow: '0 1px 3px rgba(0, 0, 0, 0.3)'
            }}>
              👤 Username
            </label>
            <div className="input-container" style={{ position: 'relative' }}>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                placeholder="Enter your username..."
                required
                className="cool-input"
                style={{
                  width: '100%',
                  padding: '1.2rem 1.5rem',
                  borderRadius: '20px',
                  border: '2px solid rgba(255, 255, 255, 0.3)',
                  background: 'rgba(255, 255, 255, 0.1)',
                  color: 'white',
                  fontSize: '1rem',
                  transition: 'all 0.3s ease',
                  backdropFilter: 'blur(10px)',
                  boxSizing: 'border-box'
                }}
                onFocus={(e) => {
                  e.target.style.borderColor = 'var(--light-blue)';
                  e.target.style.boxShadow = '0 0 20px rgba(135, 206, 235, 0.4)';
                  e.target.style.transform = 'scale(1.02)';
                }}
                onBlur={(e) => {
                  e.target.style.borderColor = 'rgba(255, 255, 255, 0.3)';
                  e.target.style.boxShadow = 'none';
                  e.target.style.transform = 'scale(1)';
                }}
              />
              <div className="input-wave" style={{
                position: 'absolute',
                bottom: '0',
                left: '0',
                width: '100%',
                height: '2px',
                background: 'linear-gradient(90deg, var(--light-blue), var(--sky-blue), var(--ocean-blue))',
                borderRadius: '1px',
                transform: 'scaleX(0)',
                transformOrigin: 'left',
                transition: 'transform 0.3s ease'
              }} />
            </div>
          </div>

          {/* Password Field */}
          <div style={{ position: 'relative' }}>
            <label style={{
              display: 'block',
              fontSize: '1rem',
              fontWeight: 600,
              marginBottom: '0.8rem',
              color: 'rgba(255, 255, 255, 0.9)',
              textShadow: '0 1px 3px rgba(0, 0, 0, 0.3)'
            }}>
              🔒 Password
            </label>
            <div className="input-container" style={{ position: 'relative' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Enter your password..."
                required
                className="cool-input"
                style={{
                  width: '100%',
                  padding: '1.2rem 1.5rem',
                  paddingRight: '3.5rem',
                  borderRadius: '20px',
                  border: '2px solid rgba(255, 255, 255, 0.3)',
                  background: 'rgba(255, 255, 255, 0.1)',
                  color: 'white',
                  fontSize: '1rem',
                  transition: 'all 0.3s ease',
                  backdropFilter: 'blur(10px)',
                  boxSizing: 'border-box'
                }}
                onFocus={(e) => {
                  e.target.style.borderColor = 'var(--light-blue)';
                  e.target.style.boxShadow = '0 0 20px rgba(135, 206, 235, 0.4)';
                  e.target.style.transform = 'scale(1.02)';
                }}
                onBlur={(e) => {
                  e.target.style.borderColor = 'rgba(255, 255, 255, 0.3)';
                  e.target.style.boxShadow = 'none';
                  e.target.style.transform = 'scale(1)';
                }}
              />

              {/* Password Toggle Button */}
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="password-toggle"
                style={{
                  position: 'absolute',
                  right: '15px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none',
                  border: 'none',
                  color: 'rgba(255, 255, 255, 0.7)',
                  cursor: 'pointer',
                  fontSize: '1.2rem',
                  transition: 'all 0.3s ease',
                  padding: '5px',
                  borderRadius: '50%'
                }}
                onMouseEnter={(e) => {
                  e.target.style.color = 'var(--light-blue)';
                  e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                }}
                onMouseLeave={(e) => {
                  e.target.style.color = 'rgba(255, 255, 255, 0.7)';
                  e.target.style.background = 'none';
                }}
              >
                {showPassword ? '👁️' : '🔒'}
              </button>
            </div>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loading}
            className="login-button"
            style={{
              padding: '1.3rem',
              borderRadius: '20px',
              border: 'none',
              background: loading
                ? 'rgba(255, 255, 255, 0.1)'
                : 'linear-gradient(45deg, var(--sky-blue), var(--ocean-blue))',
              color: 'white',
              fontSize: '1.2rem',
              fontWeight: 700,
              cursor: loading ? 'not-allowed' : 'pointer',
              transition: 'all 0.3s ease',
              boxShadow: '0 10px 30px rgba(52, 152, 219, 0.3)',
              position: 'relative',
              overflow: 'hidden'
            }}
            onMouseEnter={(e) => {
              if (!loading) {
                e.target.style.transform = 'translateY(-3px) scale(1.02)';
                e.target.style.boxShadow = '0 15px 40px rgba(52, 152, 219, 0.4)';
              }
            }}
            onMouseLeave={(e) => {
              if (!loading) {
                e.target.style.transform = 'translateY(0) scale(1)';
                e.target.style.boxShadow = '0 10px 30px rgba(52, 152, 219, 0.3)';
              }
            }}
          >
            {loading ? (
              <span style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.5rem'
              }}>
                <div style={{
                  width: '20px',
                  height: '20px',
                  border: '2px solid rgba(255, 255, 255, 0.3)',
                  borderTop: '2px solid white',
                  borderRadius: '50%',
                  animation: 'spin 1s linear infinite'
                }} />
                Signing In...
              </span>
            ) : (
              '🚀 Sign In to WeatherCast'
            )}
          </button>
        </form>

        {/* Footer Links */}
        <div style={{
          marginTop: '2.5rem',
          textAlign: 'center',
          borderTop: '1px solid rgba(255, 255, 255, 0.2)',
          paddingTop: '2rem'
        }}>
          {/* Forgot Password Link */}
          <div style={{ marginBottom: '1.5rem' }}>
            <Link
              to="/forgot-password"
              style={{
                color: 'rgba(255, 255, 255, 0.8)',
                textDecoration: 'none',
                fontSize: '0.95rem',
                transition: 'all 0.3s ease',
                padding: '0.5rem',
                borderRadius: '10px'
              }}
              onMouseEnter={(e) => {
                e.target.style.color = 'var(--light-blue)';
                e.target.style.background = 'rgba(255, 255, 255, 0.1)';
              }}
              onMouseLeave={(e) => {
                e.target.style.color = 'rgba(255, 255, 255, 0.8)';
                e.target.style.background = 'transparent';
              }}
            >
              🔑 Forgot your password?
            </Link>
          </div>

          <p style={{
            color: 'rgba(255, 255, 255, 0.8)',
            fontSize: '1rem',
            marginBottom: '1rem'
          }}>
            ✨ Don't have an account yet?
          </p>
          <Link
            to="/register"
            className="register-link"
            style={{
              color: 'var(--light-blue)',
              textDecoration: 'none',
              fontSize: '1.1rem',
              fontWeight: 600,
              padding: '0.8rem 2rem',
              borderRadius: '25px',
              border: '2px solid rgba(135, 206, 235, 0.5)',
              background: 'rgba(135, 206, 235, 0.1)',
              transition: 'all 0.3s ease',
              display: 'inline-block'
            }}
            onMouseEnter={(e) => {
              e.target.style.background = 'rgba(135, 206, 235, 0.2)';
              e.target.style.transform = 'translateY(-2px)';
              e.target.style.boxShadow = '0 8px 25px rgba(135, 206, 235, 0.3)';
            }}
            onMouseLeave={(e) => {
              e.target.style.background = 'rgba(135, 206, 235, 0.1)';
              e.target.style.transform = 'translateY(0)';
              e.target.style.boxShadow = 'none';
            }}
          >
            🌟 Create Account
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
