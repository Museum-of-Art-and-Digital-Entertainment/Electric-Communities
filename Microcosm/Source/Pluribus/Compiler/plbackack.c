/*
  plbackack.c -- In case anybody asks what Pluribus stands for

  Chip Morningstar
  Electric Communities
  11-October-1995

  Copyright 1995 Electric Communities, all rights reserved.

*/

#include <stdio.h>
#include <stdlib.h>

#ifdef WIN32
#include <windows.h>
#define random() rand()
#define srandom(a) srand(a)
#define getpid() GetCurrentProcessId()
#else
#include <unistd.h>
#endif

static char *bwords[] = {
    "Babysitting",      "Bacchanalia",  "Balkanization", "Bankruptcy",
    "Baptism",          "Barratry",     "Basketry",      "Bastardization",
    "Beadwork",         "Bearbaiting",  "Beatification", "Beautification",
    "Begetting",        "Behavior",     "Behaviorism",   "Belief",
    "Belligerency",     "Benediction",  "Benefaction",   "Bestiary",
    "Bestirring",       "Bestowal",     "Betrayal",      "Betterment",
    "Bewitchment",      "Bibliography", "Bifurcation",   "Binding",
    "Biochemistry",     "Biology",      "Blandish",      "Blitzkrieg",
    "Bodybuilding",     "Boggling",     "Boilermaking",  "Boloney",
    "Bolshevism",       "Bonding",      "Bookkeeping",   "Bootlegging",
    "Bootstrapping",    "Botheration",  "Bottlenecks",   "Boundary",
    "Bowdlerization",   "Bowling",      "Braiding",      "Brainwashing",
    "Breakdown",        "Bribery",      "Bricklaying",   "Brickwork",
    "Bridgework",       "Brinkmanship", "Broadcasting",  "Brokerage",
    "Bronzing",         "Brotherhood",  "Brotherliness", "Brutalization",
    "Buddhism",         "Budgeting",    "Buggery",       "Building",
    "Bureaucracy",      "Burglary",     "Business",      "Butchery",
    "Byproducts"
};

static char *iwords[] = {
    "Iceberg",          "Icon",           "Iconography",   "Identity",
    "Idiom",            "Idiosyncrasy",   "Iguana",        "Imagination",
    "Implant",          "Implementation", "Improbability", "Incarnation",
    "Incentive",        "Inclusion",      "Income",        "Incrustation",
    "Indemnity",        "Inducement",     "Indulgence",    "Industry",
    "Infection",        "Information",    "Infundibulum",  "Inhibition",
    "Inkblot",          "Inquiry",        "Insect",        "Insight",
    "Institution",      "Instrument",     "Interest",      "Intermission",
    "Interstitial",     "Intervention",   "Invariance",    "Invention",
    "Inventory",        "Investment",     "Invoice",       "Ion",
    "Ironware",         "Island",         "Isotope",       "Item",
    "Ingredient"
};

static char *rwords[] = {
    "Randomization",    "Ratification",   "Ratiocination",  "Rationalization",
    "Ravelment",        "Razzmatazz",     "Reactance",      "Reactivation",
    "Readiness",        "Realization",    "Realty",         "Reassurance",
    "Rebirth",          "Recherche",      "Recipes",        "Recognition",
    "Recombination",    "Reconstruction", "Recovery",       "Recreation",
    "Recursion",        "Redaction",      "Redecoration",   "Rededication",
    "Redemption",       "Redeployment",   "Redesign",       "Redetermination",
    "Redevelopment",    "Redirection",    "Redistribution", "Reductionism",
    "Reference",        "Refinement",     "Reformation",    "Refrigeration",
    "Refulgence",       "Refurbishment",  "Regeneration",   "Regimentation",
    "Registration",     "Regression",     "Regrouping",     "Regulation",
    "Regurgitation",    "Reification",    "Reinforcement",  "Rejuvenation",
    "Relation",         "Relationships",  "Relativity",     "Releasability",
    "Relevance",        "Reliability",    "Remuneration",   "Rendition",
    "Reorganization",   "Repair",         "Repentance",     "Replacement",
    "Replication",      "Reportage",      "Representation", "Repression",
    "Reproduction",     "Repudiation",    "Reputability",   "Reputation",
    "Requirements",     "Requisition",    "Reservation",    "Resistivity",
    "Resolution",       "Resonance",      "Resources",      "Respectability",
    "Respiration",      "Resplendence",   "Responsibility", "Restitution",
    "Restoration",      "Restriction",    "Resurrection",   "Resuscitation",
    "Retrenchment",     "Retribution",    "Retrospection",  "Reuse",
    "Revelation",       "Reverberation",  "Reversibility",  "Revisionism",
    "Revitalization",   "Revocation",     "Revolution",     "Rhetoric",
    "Rhythmization",    "Ridicule",       "Rigamarole",     "Righteousness",
    "Riskiness",        "Ritualization",  "Robbery",        "Robotics",
    "Robotization",     "Rocketry",       "Rodenticide",    "Romance",
    "Roofing",          "Rotation",       "Roughage",       "Ruggedization",
    "Russification",    "Rustication"
};

static char *swords[] = {
    "Sabotage",         "Sacrifice",      "Salability",    "Saltation",
    "Salutation",       "Salvation",      "Sanctification","Sanitation",
    "Sarcasm",          "Satisfaction",   "Satrapy",       "Savagery",
    "Scalability",      "Scatology",      "Schematization","Scholarship",
    "Schooling",        "Sclerosis",      "Scope",         "Scorching",
    "Scouring",         "Scrawling",      "Scribbling",    "Scrounging",
    "Scrutiny",         "Scullery",       "Sculpture",     "Searching",
    "Seasickness",      "Seasoning",      "Seating",       "Seaworthiness",
    "Seclusion",        "Secrecy",        "Sectionalism",  "Secularization",
    "Sedation",         "Sedimentation",  "Sedition",      "Seduction",
    "Seeding",          "Seemliness",     "Segmentation",  "Segregation",
    "Seismology",       "Selection",      "Semantics",     "Seniority",
    "Sensationalism",   "Sensitivity",    "Sensualization","Sentience",
    "Sentimentalism",   "Separation",     "Sequencing",    "Sequestration",
    "Serendipity",      "Serialization",  "Service",       "Serviceability",
    "Setup",            "Severability",   "Sexuality",     "Shakedown",
    "Shakiness",        "Sharpening",     "Sheepdip",      "Shellacking",
    "Shipfitting",      "Shoddiness",     "Siegecraft",    "Signification",
    "Silliness",        "Simplification", "Simulation",    "Sincerity",
    "Singularity",      "Situation",      "Skepticism",    "Skullduggery",
    "Slaughter",        "Slavery",        "Sleaziness",    "Sleepiness",
    "Slippage",         "Slipperiness",   "Smelting",      "Sneakiness",
    "Snobbery",         "Socialism",      "Socialization", "Sociology",
    "Solemnization",    "Solicitation",   "Solidification","Solubility",
    "Somnambulism",     "Sophistry",      "Soundproofing", "Sovereignty",
    "Spanking",         "Specialization", "Speciation",    "Specification",
    "Speculation",      "Spiciness",      "Spindling",     "Spiritualism",
    "Spontaneity",      "Squishiness",    "Stabilization", "Stagecraft",
    "Stakhanovism",     "Standardization","Startup",       "Stateliness",
    "Statistics",       "Steadiness",     "Stealthiness",  "Steering",
    "Stenography",      "Stereophotography","Sterilization","Stiffening",
    "Stigmatization",   "Stimulation",    "Stipulation",   "Stoniness",
    "Stoppage",         "Storage",        "Stowage",       "Strangulation",
    "Strategy",         "Stratification", "Striation",     "Stringiness",
    "Structuralization","Stumpage",       "Stupefaction",  "Styling",
    "Stylization",      "Subagency",      "Subassembly",   "Subcontracting",
    "Subduction",       "Subjectivism",   "Subjugation",   "Subjunction",
    "Submission",       "Subservience",   "Subsidization", "Subsistence",
    "Substantiation",   "Substitution",   "Subsumption",   "Subtraction",
    "Subversion",       "Succession",     "Succulence",    "Suffrage",
    "Suggestion",       "Suitability",    "Summarization", "Superconductivity",
    "Superficiality",   "Superfluity",    "Superintendence","Superintendency",
    "Superiority",      "Superstition",   "Supervision",   "Supplication",
    "Support",          "Supposition",    "Suppression",   "Supremacy",
    "Surfacing",        "Surgery",        "Surrender",     "Surrogacy",
    "Surveillance",     "Sustenance",     "Swashbuckling", "Switching",
    "Symbolization",    "Symptomatology", "Synaesthesis",  "Synchronization",
    "Synonymization",   "Syntax",         "Systematics",   "Systematism",
    "Systematization",  "Systemization"
};

static char *uwords[] = {
    "Undercarriage",    "Undergrowth",  "Underworld",   "Unicorn",
    "Unicycle",         "Union",        "Universe",     "Unreality",
    "Unum",             "Upgrade",      "Upholstery",   "Utensil",
    "Utility"
};

#define WSIZE(arr)   (sizeof(arr) / sizeof(char *))
#define ROLL(limit)  (abs(random()) % (limit))

  void
backack(void)
{
    int u1, r, i, b, u2, s;

    srandom(getpid());
    u1 = ROLL(WSIZE(uwords));
    r  = ROLL(WSIZE(rwords));
    i  = ROLL(WSIZE(iwords));
    b  = ROLL(WSIZE(bwords));
    do {
        u2 = ROLL(WSIZE(uwords));
    } while (u2 == u1);
    s  = ROLL(WSIZE(swords));
    printf("Programming Language for %s %s, %s %s, and %s %s\n",
           uwords[u1], rwords[r], iwords[i], bwords[b], uwords[u2], swords[s]);
}
