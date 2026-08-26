/**
 * Wallpaper Engine Landing Page JS
 * - Interactive Strategy Simulator
 * - Cosmic Nebula Canvas Shader Effect
 * - Phone Mockup Wallpaper Cycler
 */

document.addEventListener('DOMContentLoaded', () => {
    initCanvasShader();
    initStrategySimulator();
    initHeroWallpaperCycler();
});

/* -------------------------------------------------------------
 * 1. Background Cosmic Nebula Particle Shader
 * ----------------------------------------------------------- */
function initCanvasShader() {
    const canvas = document.getElementById('bg-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    let width, height;
    let particles = [];
    const particleCount = 45;

    function resize() {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    }
    window.addEventListener('resize', resize);
    resize();

    class Particle {
        constructor() {
            this.reset();
        }
        reset() {
            this.x = Math.random() * width;
            this.y = Math.random() * height;
            this.radius = Math.random() * 140 + 60;
            this.vx = (Math.random() - 0.5) * 0.3;
            this.vy = (Math.random() - 0.5) * 0.3;
            this.hue = Math.random() > 0.5 ? 260 : 190; // Violet or Cyan
            this.alpha = Math.random() * 0.08 + 0.03;
        }
        update() {
            this.x += this.vx;
            this.y += this.vy;
            if (this.x < -this.radius) this.x = width + this.radius;
            if (this.x > width + this.radius) this.x = -this.radius;
            if (this.y < -this.radius) this.y = height + this.radius;
            if (this.y > height + this.radius) this.y = -this.radius;
        }
        draw() {
            const gradient = ctx.createRadialGradient(
                this.x, this.y, 0,
                this.x, this.y, this.radius
            );
            gradient.addColorStop(0, `hsla(${this.hue}, 90%, 60%, ${this.alpha})`);
            gradient.addColorStop(1, 'transparent');
            ctx.fillStyle = gradient;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
            ctx.fill();
        }
    }

    for (let i = 0; i < particleCount; i++) {
        particles.push(new Particle());
    }

    function animate() {
        ctx.clearRect(0, 0, width, height);
        // Deep space background fill
        ctx.fillStyle = '#050508';
        ctx.fillRect(0, 0, width, height);

        for (let p of particles) {
            p.update();
            p.draw();
        }
        requestAnimationFrame(animate);
    }
    animate();
}

/* -------------------------------------------------------------
 * 2. Interactive Strategy Simulator
 * ----------------------------------------------------------- */
const STRATEGY_DATA = {
    SMART_SHUFFLE: {
        title: "Cyberpunk Obsidian Grid",
        tags: "Cyberpunk • Energetic • 4.9★",
        bg: "radial-gradient(circle at 70% 20%, #7C3AED 0%, #1E1B4B 50%, #050508 90%)",
        reason: "\"Score 9.12: High favorite weighting (+3.0), non-repeated color hash, optimal evening luminance matched.\"",
        lum: "0.19",
        oled: "84%",
        rate: "4.9★",
        prio: "CRITICAL"
    },
    TIME_OF_DAY: {
        title: "Twilight Horizon Glow",
        tags: "Minimalist • Serene • 4.7★",
        bg: "radial-gradient(circle at 40% 40%, #06B6D4 0%, #0F172A 60%, #020617 95%)",
        reason: "\"Calculated by NOAA Solar Engine: Sunset detected (20:45). Soft ambient cyan matched to twilight profile.\"",
        lum: "0.26",
        oled: "76%",
        rate: "4.7★",
        prio: "HIGH"
    },
    BATTERY_SAVER: {
        title: "Pure OLED Monochrome Void",
        tags: "Monochrome • Mysterious • 4.5★",
        bg: "radial-gradient(circle at 50% 50%, #18181B 0%, #000000 70%)",
        reason: "\"Context Trigger: Battery Saver Mode active (<20%). High-contrast pure black floor (#000000) selected to save power.\"",
        lum: "0.05",
        oled: "96%",
        rate: "4.5★",
        prio: "REALTIME"
    },
    VARIETY: {
        title: "Sakura Neon Dream",
        tags: "Anime • Ethereal • 4.6★",
        bg: "radial-gradient(circle at 60% 70%, #DB2777 0%, #31102A 50%, #050508 90%)",
        reason: "\"Variety Matrix: Previous wallpaper was Cyberpunk Blue. Switching style genre to Anime Ethereal for visual diversity.\"",
        lum: "0.22",
        oled: "79%",
        rate: "4.6★",
        prio: "NORMAL"
    },
    WEIGHTED_FAVORITES: {
        title: "Electric Aurora Pulse",
        tags: "Abstract • Vibrant • 5.0★",
        bg: "radial-gradient(circle at 30% 60%, #10B981 0%, #064E3B 50%, #022C22 90%)",
        reason: "\"75/25 Weighted Pool: User has 5★ favorited this item with 0 skips over 30 days. Priority boosted to top tier.\"",
        lum: "0.24",
        oled: "81%",
        rate: "5.0★",
        prio: "BOOSTED"
    },
    NEVER_REPEAT: {
        title: "Deep Space Nebulae V",
        tags: "Sci-Fi • Cosmic • 4.4★",
        bg: "radial-gradient(circle at 50% 30%, #4F46E5 0%, #1E1B4B 60%, #050508 95%)",
        reason: "\"Anti-Thrashing: Not seen in 42 days. Rotated from oldest unviewed quadrant of your 300+ library.\"",
        lum: "0.15",
        oled: "88%",
        rate: "4.4★",
        prio: "DISCOVERY"
    }
};

function initStrategySimulator() {
    const chips = document.querySelectorAll('.strat-chip');
    const titleEl = document.getElementById('sim-title');
    const tagsEl = document.getElementById('sim-tags');
    const bgEl = document.getElementById('sim-img-bg');
    const reasonEl = document.getElementById('sim-reason');
    const lumEl = document.getElementById('sim-lum');
    const oledEl = document.getElementById('sim-oled');
    const rateEl = document.getElementById('sim-rate');
    const prioEl = document.getElementById('sim-prio');
    const rotateBtn = document.getElementById('btn-sim-rotate');

    let currentStrat = 'SMART_SHUFFLE';

    function applyStrategy(stratKey) {
        currentStrat = stratKey;
        const data = STRATEGY_DATA[stratKey] || STRATEGY_DATA.SMART_SHUFFLE;

        chips.forEach(c => {
            if (c.dataset.strat === stratKey) {
                c.classList.add('active');
            } else {
                c.classList.remove('active');
            }
        });

        if (titleEl) titleEl.textContent = data.title;
        if (tagsEl) tagsEl.textContent = data.tags;
        if (bgEl) bgEl.style.background = data.bg;
        if (reasonEl) reasonEl.textContent = data.reason;
        if (lumEl) lumEl.textContent = data.lum;
        if (oledEl) oledEl.textContent = data.oled;
        if (rateEl) rateEl.textContent = data.rate;
        if (prioEl) prioEl.textContent = data.prio;
    }

    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            applyStrategy(chip.dataset.strat);
        });
    });

    if (rotateBtn) {
        rotateBtn.addEventListener('click', () => {
            const keys = Object.keys(STRATEGY_DATA);
            const nextKey = keys[(keys.indexOf(currentStrat) + 1) % keys.length];
            applyStrategy(nextKey);
        });
    }
}

/* -------------------------------------------------------------
 * 3. Hero Phone Mockup Live Wallpaper Cycler
 * ----------------------------------------------------------- */
function initHeroWallpaperCycler() {
    const wallBg = document.getElementById('screen-wallpaper-bg');
    const wallTitle = document.getElementById('hero-wall-title');
    const wallSub = document.getElementById('hero-wall-sub');
    const badge = document.getElementById('widget-strategy-badge');

    const heroCycle = [
        {
            bg: "radial-gradient(circle at 60% 30%, #5B21B6 0%, #1E1B4B 40%, #06060A 90%)",
            title: "Neon Cyber Skyline",
            sub: "Cyberpunk • Energetic • OLED Safe",
            badge: "SMART SHUFFLE"
        },
        {
            bg: "radial-gradient(circle at 30% 70%, #06B6D4 0%, #0369A1 50%, #020617 90%)",
            title: "Obsidian Twilight Flow",
            sub: "Minimalist • Serene • Solar Matched",
            badge: "SOLAR SUNSET"
        },
        {
            bg: "radial-gradient(circle at 50% 50%, #BE185D 0%, #500724 50%, #050508 90%)",
            title: "Sakura Petals Distortion",
            sub: "Anime • Ethereal • AGSL Live Shader",
            badge: "LIVE SHADER"
        }
    ];

    let index = 0;
    setInterval(() => {
        index = (index + 1) % heroCycle.length;
        const current = heroCycle[index];
        if (wallBg) wallBg.style.background = current.bg;
        if (wallTitle) wallTitle.textContent = current.title;
        if (wallSub) wallSub.textContent = current.sub;
        if (badge) badge.textContent = current.badge;
    }, 4500);
}
