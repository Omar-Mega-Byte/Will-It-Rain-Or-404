import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
  const [raindrops, setRaindrops] = useState([]);
  const [formStep, setFormStep] = useState(1);
  const [passwordValidation, setPasswordValidation] = useState({
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    special: false,
    match: false
  });
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
    // Generate animated raindrops
    const raindropInterval = setInterval(() => {
      const newRaindrop = {
        id: Date.now() + Math.random(),
        x: Math.random() * 100,
        delay: Math.random() * 2,
        size: Math.random() * 3 + 1
      };
      setRaindrops(prev => [...prev.slice(-15), newRaindrop]);
    }, 300);

    return () => clearInterval(raindropInterval);
  }, []);

  useEffect(() => {
    const password = formData.password;
    const confirmPassword = formData.confirmPassword;

    setPasswordValidation({
      length: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /\d/.test(password),
      special: /[@$!%*?&_]/.test(password),
      match: password === confirmPassword && password.length > 0 && confirmPassword.length > 0
    });
  }, [formData.password, formData.confirmPassword]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validate passwords
    const allValidationsPassed = Object.values(passwordValidation).every(Boolean);
    if (!allValidationsPassed) {
      setError('Please ensure all password requirements are met');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await authService.register(
        formData.username,
        formData.email,
        formData.password,
        formData.firstName,
        formData.lastName
      );
      navigate('/login');
    } catch (error) {
      setError(error);
    } finally {
      setLoading(false);
    }
  };

  const nextStep = () => {
    if (formStep === 1) {
      if (!formData.firstName || !formData.lastName || !formData.email) {
        setError('Please fill in all personal information');
        return;
      }
    }
    setFormStep(formStep + 1);
    setError('');
  };

  const prevStep = () => {
    setFormStep(formStep - 1);
    setError('');
  };

  return (
    <div className="register-container" style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, var(--ocean-blue) 0%, var(--deep-blue) 40%, var(--navy) 80%, var(--midnight) 100%)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem',
      position: 'relative',
      overflow: 'hidden'
    }}>

      {/* Animated Raindrops */}
      {raindrops.map(raindrop => (
        <div
          key={raindrop.id}
          className="raindrop"
          style={{
            position: 'absolute',
            left: `${raindrop.x}%`,
            top: '-5px',
            width: `${raindrop.size}px`,
            height: `${raindrop.size * 10}px`,
            background: 'linear-gradient(180deg, transparent, rgba(135, 206, 235, 0.8), transparent)',
            borderRadius: '50px',
            pointerEvents: 'none',
            animation: `raindropFall 3s linear infinite`,
            animationDelay: `${raindrop.delay}s`,
            opacity: 0.7
          }}
        />
      ))}

      {/* Interactive mouse glow */}
      <div
        className="mouse-glow"
        style={{
          position: 'absolute',
          left: `${mousePosition.x}%`,
          top: `${mousePosition.y}%`,
          width: '250px',
          height: '250px',
          background: 'radial-gradient(circle, rgba(93, 173, 226, 0.15) 0%, transparent 70%)',
          borderRadius: '50%',
          transform: 'translate(-50%, -50%)',
          pointerEvents: 'none',
          transition: 'all 0.3s ease-out'
        }}
      />

      {/* Main registration form */}
      <div className="register-form-container" style={{
        background: 'rgba(255, 255, 255, 0.12)',
        backdropFilter: 'blur(25px)',
        borderRadius: '30px',
        border: '1px solid rgba(255, 255, 255, 0.25)',
        boxShadow: '0 25px 50px rgba(0, 0, 0, 0.25)',
        width: '100%',
        maxWidth: '500px',
        padding: '3.5rem',
        position: 'relative',
        zIndex: 2,
        animation: 'slideInUp 0.8s ease-out'
      }}>

        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
          <div className="app-icon" style={{
            width: '100px',
            height: '100px',
            background: 'linear-gradient(45deg, var(--ocean-blue), var(--deep-blue))',
            borderRadius: '50%',
            margin: '0 auto 1.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 15px 35px rgba(52, 152, 219, 0.4)',
            animation: 'pulseGlow 2s infinite',
            fontSize: '3rem',
            position: 'relative',
            overflow: 'hidden'
          }}>
            <span style={{ position: 'relative', zIndex: 2 }}>⛈️</span>
            <div style={{
              position: 'absolute',
              top: '0',
              left: '0',
              width: '100%',
              height: '100%',
              background: 'conic-gradient(from 0deg, transparent, rgba(255, 255, 255, 0.2), transparent)',
              animation: 'rotate 3s linear infinite'
            }} />
          </div>

          <h1 style={{
            fontSize: '2.8rem',
            fontWeight: 900,
            background: 'linear-gradient(45deg, #FFFFFF 0%, var(--sky-blue) 50%, #FFFFFF 100%)',
            backgroundClip: 'text',
            WebkitBackgroundClip: 'text',
            color: 'transparent',
            marginBottom: '0.5rem',
            textShadow: '0 0 30px rgba(255, 255, 255, 0.3)'
          }}>
            ⛈️ Join WeatherCast
          </h1>

          <p style={{
            color: 'rgba(255, 255, 255, 0.9)',
            fontSize: '1.2rem',
            fontWeight: 500,
            textShadow: '0 2px 10px rgba(0, 0, 0, 0.3)'
          }}>
            🌟 Start your weather prediction journey
          </p>

          {/* Step Indicator */}
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            gap: '1rem',
            marginTop: '2rem'
          }}>
            {[1, 2].map(step => (
              <div
                key={step}
                style={{
                  width: '12px',
                  height: '12px',
                  borderRadius: '50%',
                  background: formStep >= step
                    ? 'linear-gradient(45deg, var(--light-blue), var(--sky-blue))'
                    : 'rgba(255, 255, 255, 0.3)',
                  transition: 'all 0.3s ease',
                  boxShadow: formStep >= step ? '0 0 15px rgba(135, 206, 235, 0.5)' : 'none'
                }}
              />
            ))}
          </div>
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

          {/* Step 1: Personal Information */}
          {formStep === 1 && (
            <div className="form-step">
              <h3 style={{
                color: 'white',
                fontSize: '1.5rem',
                marginBottom: '1.5rem',
                textAlign: 'center'
              }}>
                👤 Personal Information
              </h3>

              <div style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '1rem',
                marginBottom: '1rem'
              }}>
                <div>
                  <label style={{
                    display: 'block',
                    fontSize: '0.95rem',
                    fontWeight: 600,
                    marginBottom: '0.5rem',
                    color: 'rgba(255, 255, 255, 0.9)'
                  }}>
                    First Name
                  </label>
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    placeholder="John"
                    required
                    className="cool-input"
                    style={{
                      width: '100%',
                      padding: '1rem',
                      borderRadius: '15px',
                      border: '2px solid rgba(255, 255, 255, 0.3)',
                      background: 'rgba(255, 255, 255, 0.1)',
                      color: 'white',
                      fontSize: '0.95rem',
                      transition: 'all 0.3s ease',
                      backdropFilter: 'blur(10px)',
                      boxSizing: 'border-box'
                    }}
                  />
                </div>

                <div>
                  <label style={{
                    display: 'block',
                    fontSize: '0.95rem',
                    fontWeight: 600,
                    marginBottom: '0.5rem',
                    color: 'rgba(255, 255, 255, 0.9)'
                  }}>
                    Last Name
                  </label>
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    placeholder="Doe"
                    required
                    className="cool-input"
                    style={{
                      width: '100%',
                      padding: '1rem',
                      borderRadius: '15px',
                      border: '2px solid rgba(255, 255, 255, 0.3)',
                      background: 'rgba(255, 255, 255, 0.1)',
                      color: 'white',
                      fontSize: '0.95rem',
                      transition: 'all 0.3s ease',
                      backdropFilter: 'blur(10px)',
                      boxSizing: 'border-box'
                    }}
                  />
                </div>
              </div>

              <div>
                <label style={{
                  display: 'block',
                  fontSize: '1rem',
                  fontWeight: 600,
                  marginBottom: '0.8rem',
                  color: 'rgba(255, 255, 255, 0.9)'
                }}>
                  📧 Email Address
                </label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="john.doe@example.com"
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
                />
              </div>

              <button
                type="button"
                onClick={nextStep}
                className="next-button"
                style={{
                  padding: '1.2rem',
                  borderRadius: '20px',
                  border: 'none',
                  background: 'linear-gradient(45deg, var(--sky-blue), var(--ocean-blue))',
                  color: 'white',
                  fontSize: '1.1rem',
                  fontWeight: 700,
                  cursor: 'pointer',
                  transition: 'all 0.3s ease',
                  boxShadow: '0 10px 30px rgba(52, 152, 219, 0.3)',
                  marginTop: '1rem'
                }}
              >
                Continue ➡️
              </button>
            </div>
          )}

          {/* Step 2: Account Security */}
          {formStep === 2 && (
            <div className="form-step">
              <h3 style={{
                color: 'white',
                fontSize: '1.5rem',
                marginBottom: '1.5rem',
                textAlign: 'center'
              }}>
                🔐 Account Security
              </h3>

              <div>
                <label style={{
                  display: 'block',
                  fontSize: '1rem',
                  fontWeight: 600,
                  marginBottom: '0.8rem',
                  color: 'rgba(255, 255, 255, 0.9)'
                }}>
                  👤 Username
                </label>
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  placeholder="Choose a unique username"
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
                />
              </div>

              <div>
                <label style={{
                  display: 'block',
                  fontSize: '1rem',
                  fontWeight: 600,
                  marginBottom: '0.8rem',
                  color: 'rgba(255, 255, 255, 0.9)'
                }}>
                  🔒 Password
                </label>
                <div style={{ position: 'relative' }}>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="Create a strong password"
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
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    style={{
                      position: 'absolute',
                      right: '15px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      background: 'none',
                      border: 'none',
                      color: 'rgba(255, 255, 255, 0.7)',
                      cursor: 'pointer',
                      fontSize: '1.2rem'
                    }}
                  >
                    {showPassword ? '👁️' : '🔒'}
                  </button>
                </div>
              </div>

              <div>
                <label style={{
                  display: 'block',
                  fontSize: '1rem',
                  fontWeight: 600,
                  marginBottom: '0.8rem',
                  color: 'rgba(255, 255, 255, 0.9)'
                }}>
                  🔒 Confirm Password
                </label>
                <div style={{ position: 'relative' }}>
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder="Confirm your password"
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
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    style={{
                      position: 'absolute',
                      right: '15px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      background: 'none',
                      border: 'none',
                      color: 'rgba(255, 255, 255, 0.7)',
                      cursor: 'pointer',
                      fontSize: '1.2rem'
                    }}
                  >
                    {showConfirmPassword ? '👁️' : '🔒'}
                  </button>
                </div>
              </div>

              {/* Password Requirements */}
              <div className="password-requirements" style={{
                background: 'rgba(255, 255, 255, 0.08)',
                borderRadius: '15px',
                padding: '1.5rem',
                marginBottom: '1rem'
              }}>
                <h4 style={{
                  color: 'rgba(255, 255, 255, 0.9)',
                  fontSize: '1rem',
                  marginBottom: '1rem'
                }}>
                  Password Requirements:
                </h4>
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 1fr',
                  gap: '0.5rem',
                  fontSize: '0.9rem'
                }}>
                  {[
                    { key: 'length', text: '8+ characters' },
                    { key: 'uppercase', text: 'Uppercase letter' },
                    { key: 'lowercase', text: 'Lowercase letter' },
                    { key: 'number', text: 'Number' },
                    { key: 'special', text: 'Special character' },
                    { key: 'match', text: 'Passwords match' }
                  ].map(requirement => (
                    <div
                      key={requirement.key}
                      style={{
                        color: passwordValidation[requirement.key] ? 'var(--light-blue)' : 'rgba(255, 255, 255, 0.5)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.5rem'
                      }}
                    >
                      <span>{passwordValidation[requirement.key] ? '✅' : '❌'}</span>
                      {requirement.text}
                    </div>
                  ))}
                </div>
              </div>

              {/* Navigation Buttons */}
              <div style={{
                display: 'flex',
                gap: '1rem',
                marginTop: '1rem'
              }}>
                <button
                  type="button"
                  onClick={prevStep}
                  className="prev-button"
                  style={{
                    flex: 1,
                    padding: '1.2rem',
                    borderRadius: '20px',
                    border: '2px solid rgba(255, 255, 255, 0.3)',
                    background: 'transparent',
                    color: 'white',
                    fontSize: '1.1rem',
                    fontWeight: 600,
                    cursor: 'pointer',
                    transition: 'all 0.3s ease'
                  }}
                >
                  ⬅️ Back
                </button>

                <button
                  type="submit"
                  disabled={loading || !Object.values(passwordValidation).every(Boolean)}
                  className="register-button"
                  style={{
                    flex: 2,
                    padding: '1.2rem',
                    borderRadius: '20px',
                    border: 'none',
                    background: (loading || !Object.values(passwordValidation).every(Boolean))
                      ? 'rgba(255, 255, 255, 0.1)'
                      : 'linear-gradient(45deg, var(--sky-blue), var(--ocean-blue))',
                    color: 'white',
                    fontSize: '1.1rem',
                    fontWeight: 700,
                    cursor: (loading || !Object.values(passwordValidation).every(Boolean)) ? 'not-allowed' : 'pointer',
                    transition: 'all 0.3s ease',
                    boxShadow: '0 10px 30px rgba(52, 152, 219, 0.3)',
                    opacity: (loading || !Object.values(passwordValidation).every(Boolean)) ? 0.6 : 1
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
                      Creating Account...
                    </span>
                  ) : (
                    '🚀 Create Account'
                  )}
                </button>
              </div>
            </div>
          )}
        </form>

        {/* Footer Links */}
        <div style={{
          marginTop: '2.5rem',
          textAlign: 'center',
          borderTop: '1px solid rgba(255, 255, 255, 0.2)',
          paddingTop: '2rem'
        }}>
          <p style={{
            color: 'rgba(255, 255, 255, 0.8)',
            fontSize: '1rem',
            marginBottom: '1rem'
          }}>
            ✨ Already have an account?
          </p>
          <Link
            to="/login"
            className="login-link"
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
              e.target.style.boxShadow = '0 10px 25px rgba(135, 206, 235, 0.3)';
            }}
            onMouseLeave={(e) => {
              e.target.style.background = 'rgba(135, 206, 235, 0.1)';
              e.target.style.transform = 'translateY(0)';
              e.target.style.boxShadow = 'none';
            }}
          >
            🌟 Sign In
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
