/**
 * Wallpaper Engine — Cinematic Product Landing Page JavaScript
 * - High-Performance Canvas AGSL-Style Nebula Shader
 * - Interactive Smart Rotation Simulator
 * - Interactive UI Preview Gallery Tabs
 * - Hero Phone Wallpaper Cycler
 * - 6-Step Pipeline Visualizer Auto-Cycle
 */

document.addEventListener('DOMContentLoaded', () => {
    initCanvasShader();
    initStrategySimulator();
    initHeroWallpaperCycler();
    initScreenMockupTabs();
    initPipelineAutoCycle();
});

/* -------------------------------------------------------------
 * 1. AGSL-Style Background Cosmic Nebula Canvas Shader
 * ----------------------------------------------------------- */
function initCanvasShader() {
    const canvas = document.getElementById('bg-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    let width, height;
    let particles = [];
    const particleCount = 42;

    function resize() {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    }
    window.addEventListener('resize', resize);
    resize();

    class CosmicParticle {
        constructor() {
            this.reset();
        }
        reset() {
            this.x = Math.random() * width;
            this.y = Math.random() * height;
            this.radius = Math.random() * 160 + 80;
            this.vx = (Math.random() - 0.5) * 0.25;
            this.vy = (Math.random() - 0.5) * 0.25;
            // 265 = Violet, 195 = Cyan, 310 = Magenta
            const colors = [265, 195, 310];
            this.hue = colors[Math.floor(Math.random() * colors.length)];
            this.alpha = Math.random() * 0.07 + 0.025;
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
            gradient.addColorStop(0, `hsla(${this.hue}, 95%, 65%, ${this.alpha})`);
            gradient.addColorStop(0.6, `hsla(${this.hue}, 85%, 50%, ${this.alpha * 0.4})`);
            gradient.addColorStop(1, 'transparent');
            ctx.fillStyle = gradient;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
            ctx.fill();
        }
    }

    for (let i = 0; i < particleCount; i++) {
        particles.push(new CosmicParticle());
    }

    function render() {
        ctx.clearRect(0, 0, width, height);
        // Base dark space fill
        ctx.fillStyle = '#030305';
        ctx.fillRect(0, 0, width, height);

        for (let p of particles) {
            p.update();
            p.draw();
        }
        requestAnimationFrame(render);
    }
    render();
}

/* -------------------------------------------------------------
 * 2. Interactive Strategy Simulator Engine
 * ----------------------------------------------------------- */
const STRATEGY_DATA = {
    SMART_SHUFFLE: {
        title: "Cyberpunk Obsidian Grid",
        tags: "Cyberpunk • Energetic • 4.9★",
        bg: "radial-gradient(circle at 70% 30%, #7C3AED 0%, #1E1B4B 50%, #050508 90%)",
        reason: "\"Score 9.12: High favorite weighting (+3.0), non-repeated color hash, optimal evening luminance matched.\"",
        lum: "0.19",
        oled: "84%",
        rate: "4.9★",
        prio: "CRITICAL",
        time: "0.007s"
    },
    TIME_OF_DAY: {
        title: "Twilight Horizon Glow",
        tags: "Minimalist • Serene • Solar Dusk",
        bg: "radial-gradient(circle at 40% 40%, #06B6D4 0%, #0F172A 60%, #020617 95%)",
        reason: "\"Calculated by NOAA Solar Engine: Sunset detected (20:45). Soft ambient cyan matched to twilight profile.\"",
        lum: "0.26",
        oled: "76%",
        rate: "4.7★",
        prio: "HIGH",
        time: "0.005s"
    },
    BATTERY_SAVER: {
        title: "Pure OLED Monochrome Void",
        tags: "Monochrome • Mysterious • OLED Pure",
        bg: "radial-gradient(circle at 50% 50%, #18181B 0%, #000000 70%)",
        reason: "\"Context Trigger: Battery Saver Mode active (<20%). High-contrast pure black floor (#000000) selected to save power.\"",
        lum: "0.05",
        oled: "96%",
        rate: "4.5★",
        prio: "REALTIME",
        time: "0.003s"
    },
    VARIETY: {
        title: "Sakura Neon Dream",
        tags: "Anime • Ethereal • 4.6★",
        bg: "radial-gradient(circle at 60% 70%, #DB2777 0%, #31102A 50%, #050508 90%)",
        reason: "\"Variety Matrix: Previous wallpaper was Cyberpunk Blue. Switching style genre to Anime Ethereal for visual diversity.\"",
        lum: "0.22",
        oled: "79%",
        rate: "4.6★",
        prio: "NORMAL",
        time: "0.009s"
    },
    WEIGHTED_FAVORITES: {
        title: "Electric Aurora Pulse",
        tags: "Abstract • Vibrant • 5.0★",
        bg: "radial-gradient(circle at 30% 60%, #10B981 0%, #064E3B 50%, #022C22 90%)",
        reason: "\"75/25 Weighted Pool: User has 5★ favorited this item with 0 skips over 30 days. Priority boosted to top tier.\"",
        lum: "0.24",
        oled: "81%",
        rate: "5.0★",
        prio: "BOOSTED",
        time: "0.006s"
    },
    NEVER_REPEAT: {
        title: "Deep Space Nebulae V",
        tags: "Sci-Fi • Cosmic • 4.4★",
        bg: "radial-gradient(circle at 50% 30%, #4F46E5 0%, #1E1B4B 60%, #050508 95%)",
        reason: "\"Anti-Thrashing: Not seen in 42 days. Rotated from oldest unviewed quadrant of your 300+ library.\"",
        lum: "0.15",
        oled: "88%",
        rate: "4.4★",
        prio: "DISCOVERY",
        time: "0.008s"
    }
};

function initStrategySimulator() {
    const buttons = document.querySelectorAll('.strat-btn');
    const titleEl = document.getElementById('sim-title');
    const tagsEl = document.getElementById('sim-tags');
    const bgEl = document.getElementById('sim-img-bg');
    const reasonEl = document.getElementById('sim-reason');
    const lumEl = document.getElementById('sim-lum');
    const oledEl = document.getElementById('sim-oled');
    const rateEl = document.getElementById('sim-rate');
    const prioEl = document.getElementById('sim-prio');
    const timeEl = document.getElementById('sim-timestamp');
    const rotateBtn = document.getElementById('btn-sim-rotate');

    let currentStrat = 'SMART_SHUFFLE';

    function setStrategy(key) {
        currentStrat = key;
        const d = STRATEGY_DATA[key] || STRATEGY_DATA.SMART_SHUFFLE;

        buttons.forEach(btn => {
            if (btn.dataset.strat === key) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });

        if (titleEl) titleEl.textContent = d.title;
        if (tagsEl) tagsEl.textContent = d.tags;
        if (bgEl) bgEl.style.background = d.bg;
        if (reasonEl) reasonEl.textContent = d.reason;
        if (lumEl) lumEl.textContent = d.lum;
        if (oledEl) oledEl.textContent = d.oled;
        if (rateEl) rateEl.textContent = d.rate;
        if (prioEl) prioEl.textContent = d.prio;
        if (timeEl) timeEl.textContent = d.time;
    }

    buttons.forEach(btn => {
        btn.addEventListener('click', () => {
            setStrategy(btn.dataset.strat);
        });
    });

    if (rotateBtn) {
        rotateBtn.addEventListener('click', () => {
            const keys = Object.keys(STRATEGY_DATA);
            const nextIdx = (keys.indexOf(currentStrat) + 1) % keys.length;
            setStrategy(keys[nextIdx]);
        });
    }
}

/* -------------------------------------------------------------
 * 3. Hero Phone Mockup Wallpaper Cycler
 * ----------------------------------------------------------- */
function initHeroWallpaperCycler() {
    const wallBg = document.getElementById('screen-wallpaper-bg');
    const wallTitle = document.getElementById('hero-wall-title');
    const wallSub = document.getElementById('hero-wall-sub');
    const badge = document.getElementById('widget-strategy-badge');
    const starBtn = document.getElementById('hero-widget-star');
    const nextBtn = document.getElementById('hero-widget-next');

    const heroCycle = [
        {
            bg: "radial-gradient(circle at 70% 30%, #7C3AED 0%, #1E1B4B 45%, #050508 90%)",
            title: "Neon Cyber Skyline",
            sub: "Cyberpunk • Energetic • OLED 84%",
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
        },
        {
            bg: "radial-gradient(circle at 40% 60%, #10B981 0%, #064E3B 50%, #011A14 90%)",
            title: "Emerald Geometric Void",
            sub: "Abstract • Geometric • 5.0★ Starred",
            badge: "WEIGHTED FAVORITE"
        }
    ];

    let index = 0;

    function applyHero(idx) {
        index = idx % heroCycle.length;
        const item = heroCycle[index];
        if (wallBg) wallBg.style.background = item.bg;
        if (wallTitle) wallTitle.textContent = item.title;
        if (wallSub) wallSub.textContent = item.sub;
        if (badge) badge.textContent = item.badge;
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            applyHero(index + 1);
        });
    }

    if (starBtn) {
        starBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            starBtn.textContent = starBtn.textContent.includes('★') ? '☆ STAR' : '★ STARRED';
        });
    }

    setInterval(() => {
        applyHero(index + 1);
    }, 4800);
}

/* -------------------------------------------------------------
 * 4. UI Preview Gallery Tabs & Screen Switcher
 * ----------------------------------------------------------- */
const MOCK_SCREENS = {
    HOME: `
        <div class="mock-top-bar">
            <span class="mock-app-title">WALLPAPER ENGINE</span>
            <span class="mock-status-pill">✦ ACTIVE</span>
        </div>
        <div class="mock-hero-preview">
            <div class="mock-hero-img" style="background: radial-gradient(circle at 60% 40%, #7C3AED 0%, #06B6D4 50%, #050508 90%);"></div>
            <div class="mock-hero-overlay">
                <div class="mock-wall-title">Neon Cyber City 2077</div>
                <div class="mock-wall-meta">Applied • 4.9★ • Smart Shuffle</div>
            </div>
        </div>
        <div class="mock-section-label">QUICK ROTATION MODES</div>
        <div class="mock-grid-modes">
            <div class="mock-mode-chip active">✦ Smart Shuffle</div>
            <div class="mock-mode-chip">☀️ Solar Dawn</div>
            <div class="mock-mode-chip">🔋 OLED Saver</div>
            <div class="mock-mode-chip">🎨 Variety</div>
        </div>
        <div class="mock-action-btn">ROTATE NOW ❯</div>
    `,
    LIBRARY: `
        <div class="mock-top-bar">
            <span class="mock-app-title">LIBRARY (324 WALLPAPERS)</span>
            <span class="mock-status-pill">FILTERED</span>
        </div>
        <div style="display: flex; gap: 6px; margin-bottom: 12px; overflow-x: hidden;">
            <span style="font-size: 10px; background: #7C3AED; color: #FFF; padding: 4px 10px; border-radius: 12px; font-weight: 700;">ALL</span>
            <span style="font-size: 10px; background: rgba(255,255,255,0.08); color: #9CA3AF; padding: 4px 10px; border-radius: 12px;">AMOLED</span>
            <span style="font-size: 10px; background: rgba(255,255,255,0.08); color: #9CA3AF; padding: 4px 10px; border-radius: 12px;">CYBERPUNK</span>
            <span style="font-size: 10px; background: rgba(255,255,255,0.08); color: #9CA3AF; padding: 4px 10px; border-radius: 12px;">NATURE</span>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 16px;">
            <div style="height: 120px; border-radius: 12px; background: radial-gradient(circle, #7C3AED, #050508); border: 1px solid rgba(255,255,255,0.1);"></div>
            <div style="height: 120px; border-radius: 12px; background: radial-gradient(circle, #06B6D4, #020617); border: 1px solid rgba(255,255,255,0.1);"></div>
            <div style="height: 120px; border-radius: 12px; background: radial-gradient(circle, #DB2777, #050508); border: 1px solid rgba(255,255,255,0.1);"></div>
            <div style="height: 120px; border-radius: 12px; background: radial-gradient(circle, #10B981, #011A14); border: 1px solid rgba(255,255,255,0.1);"></div>
        </div>
        <div class="mock-action-btn">+ IMPORT NEW WALLPAPER</div>
    `,
    DNA: `
        <div class="mock-top-bar">
            <span class="mock-app-title">WALLPAPER DNA INSPECTOR</span>
            <span class="mock-status-pill" style="color: #06B6D4; background: rgba(6,182,212,0.2);">VERIFIED</span>
        </div>
        <div style="height: 160px; border-radius: 16px; background: radial-gradient(circle at 60% 30%, #7C3AED, #06B6D4, #050508); margin-bottom: 14px; position: relative;">
            <div style="position: absolute; bottom: 8px; left: 10px; color: #FFF; font-size: 12px; font-weight: 800;">Obsidian Cyber Skyline</div>
        </div>
        <div style="background: rgba(255,255,255,0.04); border-radius: 12px; padding: 12px; font-size: 11px; margin-bottom: 14px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
                <span>Luminance:</span> <strong style="color: #FFF;">0.19 (Dark)</strong>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
                <span>OLED Purity:</span> <strong style="color: #06B6D4;">84% True Black</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
                <span>Inferred Style:</span> <strong style="color: #A78BFA;">Cyberpunk, Sci-Fi</strong>
            </div>
        </div>
        <div class="mock-action-btn">APPLY AS LIVE WALLPAPER ✦</div>
    `,
    SCHEDULES: `
        <div class="mock-top-bar">
            <span class="mock-app-title">NOAA SOLAR AUTOMATION</span>
            <span class="mock-status-pill">ACTIVE</span>
        </div>
        <div style="background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 14px; padding: 12px; margin-bottom: 10px;">
            <div style="font-size: 12px; font-weight: 800; color: #FBBF24;">☀️ Sunrise / Dawn</div>
            <div style="font-size: 10px; color: #9CA3AF;">Rotates bright vibrant nature wallpapers at 06:12</div>
        </div>
        <div style="background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 14px; padding: 12px; margin-bottom: 10px;">
            <div style="font-size: 12px; font-weight: 800; color: #06B6D4;">🌆 Golden Hour / Sunset</div>
            <div style="font-size: 10px; color: #9CA3AF;">Rotates warm ambient tones at 19:45</div>
        </div>
        <div style="background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 14px; padding: 12px; margin-bottom: 14px;">
            <div style="font-size: 12px; font-weight: 800; color: #A78BFA;">🌙 Midnight OLED</div>
            <div style="font-size: 10px; color: #9CA3AF;">Forces pure black OLED wallpapers between 23:00 - 06:00</div>
        </div>
        <div class="mock-action-btn">+ ADD SCHEDULE RULE</div>
    `,
    EDITOR: `
        <div class="mock-top-bar">
            <span class="mock-app-title">ON-DEVICE EDITOR</span>
            <span class="mock-status-pill">NON-DESTRUCTIVE</span>
        </div>
        <div style="height: 140px; border-radius: 14px; background: radial-gradient(circle, #7C3AED, #050508); margin-bottom: 14px; border: 1px solid rgba(255,255,255,0.1);"></div>
        <div style="font-size: 10px; color: #9CA3AF; margin-bottom: 6px;">Brightness (+12)</div>
        <div style="height: 4px; background: rgba(255,255,255,0.1); border-radius: 2px; margin-bottom: 10px;"><div style="width: 60%; height: 100%; background: #7C3AED;"></div></div>
        <div style="font-size: 10px; color: #9CA3AF; margin-bottom: 6px;">OLED Black Floor Crusher (85%)</div>
        <div style="height: 4px; background: rgba(255,255,255,0.1); border-radius: 2px; margin-bottom: 16px;"><div style="width: 85%; height: 100%; background: #06B6D4;"></div></div>
        <div class="mock-action-btn">SAVE AS NEW WALLPAPER ✦</div>
    `,
    ANALYTICS: `
        <div class="mock-top-bar">
            <span class="mock-app-title">LIBRARY HEALTH & INSIGHTS</span>
            <span class="mock-status-pill" style="color: #10B981;">100% HEALTHY</span>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 14px;">
            <div style="background: rgba(255,255,255,0.04); border-radius: 12px; padding: 10px;">
                <div style="font-size: 18px; font-weight: 900; color: #FFF;">324</div>
                <div style="font-size: 9px; color: #9CA3AF;">Total Wallpapers</div>
            </div>
            <div style="background: rgba(255,255,255,0.04); border-radius: 12px; padding: 10px;">
                <div style="font-size: 18px; font-weight: 900; color: #06B6D4;">82%</div>
                <div style="font-size: 9px; color: #9CA3AF;">OLED Dark Ratio</div>
            </div>
        </div>
        <div style="background: rgba(255,255,255,0.04); border-radius: 12px; padding: 12px; font-size: 10px; margin-bottom: 14px;">
            <div style="color: #10B981; font-weight: 700; margin-bottom: 4px;">✓ 0 Broken URIs Found</div>
            <div style="color: #10B981; font-weight: 700; margin-bottom: 4px;">✓ 0 Duplicate Clusters</div>
            <div style="color: #A78BFA; font-weight: 700;">✓ Full JSON Backup Ready (1.2 MB)</div>
        </div>
        <div class="mock-action-btn">EXPORT JSON BACKUP 📦</div>
    `
};

function initScreenMockupTabs() {
    const tabs = document.querySelectorAll('.screen-tab');
    const screenCanvas = document.getElementById('mockup-screen-canvas');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            const screenKey = tab.dataset.screen;
            if (screenCanvas && MOCK_SCREENS[screenKey]) {
                screenCanvas.innerHTML = MOCK_SCREENS[screenKey];
            }
        });
    });
}

/* -------------------------------------------------------------
 * 5. 6-Step Pipeline Visualizer Auto-Cycle
 * ----------------------------------------------------------- */
function initPipelineAutoCycle() {
    const cards = document.querySelectorAll('.pipeline-card');
    if (!cards.length) return;

    let activeIndex = 0;
    setInterval(() => {
        cards.forEach(c => c.classList.remove('active'));
        activeIndex = (activeIndex + 1) % cards.length;
        cards[activeIndex].classList.add('active');
    }, 3200);
}
